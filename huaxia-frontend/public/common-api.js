(() => {
    /** ================== 基础配置 ================== */
    const BASE = "/api";
    const TOKEN_KEY = "hxytt_token";

    /** 获取 token：优先 CommonAuth，其次 localStorage */
    function getToken() {
        try {
            if (window.CommonAuth && typeof window.CommonAuth.getToken === "function") {
                const t = window.CommonAuth.getToken();
                if (t) return t;
            }
        } catch {
        }
        return localStorage.getItem(TOKEN_KEY) || "";
    }

    /** 拼 query 字符串（过滤 undefined/null/""） */
    function buildQuery(obj = {}) {
        const q = Object.entries(obj)
            .filter(([, v]) => v !== undefined && v !== null && v !== "")
            .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
            .join("&");
        return q ? `?${q}` : "";
    }

    /** 统一请求封装 */
    async function request(path, {
        method = "GET",
        query,
        headers = {},
        body,
        timeout = 30000,
        isFormData = false,      // multipart/form-data
        rawBody = false          // body 已是字符串或 FormData，不再 JSON 序列化
    } = {}) {
        const url = BASE + path + (query ? buildQuery(query) : "");
        const h = {...headers};

        const token = getToken();
        if (token) h["Authorization"] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;

        let payload = body;

        if (!rawBody && !isFormData && body && typeof body === "object") {
            h["Content-Type"] = "application/json";
            payload = JSON.stringify(body);
        }
        // isFormData=true 时让浏览器自动设置 boundary；不要手动写 Content-Type

        const ctl = new AbortController();
        const tid = setTimeout(() => ctl.abort(), timeout);

        const resp = await fetch(url, {method, headers: h, body: payload, signal: ctl.signal});
        clearTimeout(tid);

        const ct = resp.headers.get("content-type") || "";
        const data = ct.includes("application/json") ? await resp.json() : await resp.text();

        if (!resp.ok) {
            const msg = (data && (data.message || data.error)) || `HTTP ${resp.status}`;
            throw new Error(msg);
        }
        return data;
    }

    /** ================== 术语库 ================== */
    const ApiGlossary = {
        /** 创建术语库 */
        create(body) {
            // { title, sourceLanguage, targetLanguage, description }
            return request("/glossary/create", {method: "POST", body});
        },
        /** 删除术语库（Query: glossaryId） */
        delete(glossaryId) {
            return request("/glossary/delete", {method: "DELETE", query: {glossaryId}});
        },
        /** 编辑术语库 */
        edit(body) {
            // { glossaryId, title?, description? }
            return request("/glossary/edit", {method: "PUT", body});
        },
        /** 列表（分页、可按标题模糊） */
        list(params = {}) {
            // { pageNum, pageSize, title? }
            return request("/glossary/list", {method: "GET", query: params});
        },
        /** 详情（Query: glossaryId） */
        detail(glossaryId) {
            return request("/glossary/detail", {method: "GET", query: {glossaryId}});
        }
    };

    /** ================== 术语条目 ================== */
    const ApiTerm = {
        /** 新增术语 */
        add(body) {
            // { glossaryId, sourceTerm, targetTerm }
            return request("/term/add", {method: "POST", body});
        },
        /** 删除术语（单个，Query: termId） */
        delete(termId) {
            return request("/term/delete", {method: "DELETE", query: {termId}});
        },
        /** 编辑术语 */
        edit(body) {
            // { termId, sourceTerm?, targetTerm? }
            return request("/term/edit", {method: "PUT", body});
        },
        /** 查询术语（分页/关键词/按库） */
        list(params = {}) {
            // { keyword?, glossaryId?, pageSize, pageNum }
            return request("/term/list", {method: "GET", query: params});
        },
        /** 批量导入术语（上传文件） */
        addBatch({glossaryId, file}) {
            const fd = new FormData();
            fd.append("glossaryId", glossaryId);
            fd.append("file", file); // File 或 Blob
            return request("/term/add/batch", {method: "POST", isFormData: true, rawBody: true, body: fd});
        },
        /** 批量删除术语（DELETE + JSON body） */
        deleteBatch({glossaryId, termIds}) {
            // { glossaryId: number, termIds: string[] }
            return request("/term/delete/batch", {method: "DELETE", body: {glossaryId, termIds}});
        },
        /** 导出术语（获取所有数据） */
        export(params = {}) {
            // { keyword?, glossaryId? }
            return request("/term/export", {method: "GET", query: params});
        }
    };


    /** ================== 模板 ================== */
    const ApiTemplate = {
        /** 创建模板（templateName 必填，其余可选） */
        create(body) {
            // { templateName, era?, author?, textType?, style?, purpose?, audience?, translatorRole?, scene?, customRules? }
            return request("/template/create", {method: "POST", body});
        },
        /** 删除模板（Query: templateId） */
        delete(templateId) {
            return request("/template/delete", {method: "DELETE", query: {templateId}});
        },
        /** 编辑模板（templateId 必填） */
        edit(body) {
            // { templateId, templateName, era?, author?, textType?, style?, purpose?, audience?, translatorRole?, scene?, customRules? }
            return request("/template/edit", {method: "PUT", body});
        },
        /** 查询模板列表（分页 + 名称模糊） */
        list(params = {}) {
            // { pageNum, pageSize, templateName? }
            return request("/template/list", {method: "GET", query: params});
        },
        /** 模板详情（Query: templateId） */
        detail(templateId) {
            return request("/template/detail", {method: "GET", query: {templateId}});
        }
    };

    /** ================== 翻译流程（必须经过模板） ================== */
    const ApiTranslate = {
        /**
         * 创建自动机：把用户勾选的术语库 ID 列表传给后端，返回 sessionId
         * body: { glossaryIdList: number[] }
         */
        createAutomaton(glossaryIdList = []) {
            return request("/translate/automaton/create", {
                method: "POST",
                body: {glossaryIdList}
            });
        },

        /**
         * 定制化翻译（流式）——必须带 templateId；并建议带上 createAutomaton 拿到的 sessionId
         * body: { text: string, sessionId: string, templateId: number|string, selectedGlossaryCount?: number }
         *
         * 说明：
         * - 曾出现过 {text, glossaryId, templateId} 的老示例；
         *   以“创建自动机 -> 返回 sessionId -> 翻译时带 sessionId + templateId”的新版为准。
         */
        customFlux(body) {
            return request("/translate/custom/flux", {method: "POST", body});
        }
    };

    /** ================== 挂到全局（给页面直接用） ================== */
    window.CommonApi = {
        BASE,
        request,
        Glossary: ApiGlossary,
        Term: ApiTerm,
        Template: ApiTemplate,
        Translate: ApiTranslate
    };
})();

