
# 华夏译典通（前端）README

> 面向学长的交付说明：本 README 用来说明**如何在本地直接跑起来**，以及**各页面如何对接 Apifox Mock/后端微服务**。文档把前因后果、目录结构、接口映射、关键页面引入路径、调试步骤与验收要点全部写清楚。

---

## 0. 项目目标（前因后果）

- 目标：完成“术语库 / 术语 / 模板 / 翻译（流式）”等接口在前端的统一封装，保证**学长本地无后端也能直接跑**（通过 Apifox Mock 或任意后端环境），并且后续切换到真实后端时**无需改页面代码**，只需改代理地址。
- 关键设计：
  - 统一的请求层：`common-api.js` —— 固定走 `/api` 前缀，便于本地通过 **Vite 代理** 或 **Apifox 本地 Mock**。
  - 清晰的模块：术语库（Glossary）、术语（Term）、模板（Template）、翻译（Translate）。
  - 翻译流程（按学长要求 **必须选模板**）：
    1) 用户先选择一个或多个术语库 →
    2) 调用“创建自动机”接口获取 `sessionId` →
    3) 选择**模板** →
    4) 调用“定制化翻译-流式”接口提交 `text + sessionId + templateId + selectedGlossaryCount`。
- 成果：当前仓库已把**所有接口都封装成统一 JS 方法**，并**按正确的相对路径**引入各页面，可直接验证“本地可跑”。

---

## 1. 目录结构（含六大板块）

```
huaxia-V2/
├─ 传统节日/
│  └─ index.html
├─ 非遗传承/
│  └─ index.html
├─ 民俗音乐/
│  └─ index.html
├─ 诗词歌赋/
│  └─ index.html
├─ 思想流派/
│  └─ index.html
├─ 饮食文化/
│  └─ index.html
│
├─ 翻译页面/
│  ├─ css/style.css
│  ├─ script.js
│  └─ translation_page.html         ← 翻译功能入口（需用模板）
│
├─ mainpage/
│  ├─ mainpage.html
│  ├─ script.js
│  └─ style.css                     ← 主页面入口
│
├─ terminology_management/          ← 术语库/术语/模板管理
│  ├─ tailwind.config.js
│  ├─ terminology_detail.html
│  ├─ terminology_detail.js
│  ├─ terminology_management.html
│  └─ terminology_management.js
│
├─ common-api.js                    ← 统一请求层（本交付的核心）
├─ package.json
├─ vite.config.mjs                  ← 代理 /api → Apifox Mock 或后端
└─ （可选）login.html / common-auth.js
```

> **六大板块**（“传统节日/非遗传承/民俗音乐/诗词歌赋/思想流派/饮食文化”）各自只有一个 `index.html`，主要展示不同模板字段。**如需在这些页面里发起接口/跳转，也统一引入** `../common-api.js`。

---

## 2. 运行方式（本地可跑）

### 2.1 安装与启动

要求 Node.js ≥ 18

```bash
# 安装依赖
npm install

# 本地启动（默认使用 /api 代理）
npm run dev
```

打开浏览器访问 Vite 给出的本地地址（通常是 `http://localhost:5173`）。

### 2.2 代理指向 Apifox Mock 或后端

`vite.config.mjs` 里配置了一个 `TARGET`：

```js
// 例：指向 Apifox Mock（请替换为你项目里的 Base）
const TARGET = "http://127.0.0.1:4523/m1/xxxxxxxx-default";
```

- **开发/联调阶段**：把 `TARGET` 改成 **Apifox 本地 Mock 地址**，即可离线调试。
- **上线/测试阶段**：把 `TARGET` 改成 **后端网关地址**，页面代码无需改动。

> 前端所有请求都走 `/api/...`，通过代理统一转发到 `TARGET/...`。

---

## 3. 统一请求层（common-api.js）

`common-api.js` 已经把所有接口**一口气封装好**并挂到 `window`：

- `CommonApi.request`：底层请求（自动附带 `Authorization: Bearer <token>`）。
- `ApiGlossary`：创建/删除/编辑/查询/详情
- `ApiTerm`：添加/删除/批量删除/编辑/查询/（批量导入）
- `ApiTemplate`：创建/删除/编辑/查询/详情
- `ApiTranslate`：
  - `createAutomaton({ glossaryIdList })` → 返回 `sessionId`
  - `customFlux({ text, sessionId, templateId, selectedGlossaryCount })` → **流式翻译**（按学长要求必须带 `templateId`）

**引入路径要点（纠正）**  

- 在六大板块 `index.html`、`mainpage/mainpage.html`、`翻译页面/translation_page.html`、`terminology_management/*.html` 里：
  ```html
  <!-- 相对项目根目录 common-api.js 的正确写法（示例：处于子文件夹下一层） -->
  <script src="../common-api.js"></script>
  ```
  若页面位于两层或同层，请按实际层级调整 `../` 的个数。**本仓库各页面已按各自位置修正路径**。

---

## 4. 接口映射一览（与 Apifox 定义一致）

> 统一前缀均为 `/api`（由代理转发）

### 4.1 术语库（Glossary）
- `POST /glossary/create` → `ApiGlossary.create(body)`
- `DELETE /glossary/delete?glossaryId=...` → `ApiGlossary.delete(glossaryId)`
- `PUT /glossary/edit` → `ApiGlossary.edit(body)`
- `GET /glossary/list?pageNum=&pageSize=&title=` → `ApiGlossary.list(params)`
- `GET /glossary/detail?glossaryId=` → `ApiGlossary.detail(glossaryId)`

### 4.2 术语（Term）
- `POST /term/add` → `ApiTerm.add({ glossaryId, sourceTerm, targetTerm })`
- `DELETE /term/delete?termId=` → `ApiTerm.delete(termId)`
- `PUT /term/edit` → `ApiTerm.edit({ termId, sourceTerm, targetTerm })`
- `GET /term/list?keyword=&glossaryId=&pageNum=&pageSize=` → `ApiTerm.list(params)`
- `POST /term/add/batch (multipart/form-data)` → `ApiTerm.addBatch(formData)`
- `DELETE /term/delete/batch`（Body: `{ glossaryId, termIds: [] }`）→ `ApiTerm.deleteBatch(body)`

### 4.3 模板（Template）
- `POST /template/create` → `ApiTemplate.create(body)`
- `DELETE /template/delete?templateId=` → `ApiTemplate.delete(templateId)`
- `PUT /template/edit` → `ApiTemplate.edit(body)`
- `GET /template/list?pageNum=&pageSize=&templateName=` → `ApiTemplate.list(params)`
- `GET /template/detail?templateId=` → `ApiTemplate.detail(templateId)`

### 4.4 翻译（Translate）——**必须带模板**
- `POST /translate/automaton/create`（Body: `{ glossaryIdList: [int] }`）→ `ApiTranslate.createAutomaton(body)` → **返回 `sessionId`**
- `POST /translate/custom/flux`（Body: `{ text, sessionId, templateId, selectedGlossaryCount }`）→ `ApiTranslate.customFlux(body)`

所有请求默认会自动带上 `Authorization: Bearer <token>`（若 `localStorage` 中存在）。

---

## 5. 关键页面说明

### 5.1 翻译入口页：`翻译页面/translation_page.html`

- 页面顶部确保引入：
  ```html
  <script src="../common-api.js"></script>
  <script src="./script.js"></script>
  ```
- 交互流程：  
  1) 在 UI 勾选一个或多个术语库（收集 `glossaryIdList`）  
  2) 选择一个模板（拿到 `templateId`）  
  3) 先调用 `ApiTranslate.createAutomaton({ glossaryIdList })` → 拿到 `sessionId`  
  4) 再调用  
     ```js
     await ApiTranslate.customFlux({
       text,
       sessionId,
       templateId,
       selectedGlossaryCount: glossaryIdList.length
     });
     ```
  5) 展示返回的翻译结果（脚本示例已写在 `script.js`）。

> 该流程与 Apifox 截图保持一致，**不允许跳过模板**。

### 5.2 管理后台页：`terminology_management/*`
- `terminology_management.html + terminology_management.js`：术语库列表与管理入口
- `terminology_detail.html + terminology_detail.js`：某个术语库的术语增删改查、批量导入、批量删除
- 页面均已按相对路径引入 `../common-api.js`。

### 5.3 六大板块 `index.html`
- 内容是静态展示模板字段。若需要在其中跳转到“翻译页面”或发起查询，同样引用 `../common-api.js` 后即可直接使用上面封装的 API。

---

## 6. Token 与登录（可选）

- 若已接好登录页，登录成功后把后端返回的 `token` 放入 `localStorage`：
  ```js
  localStorage.setItem("hxytt_token", token);
  location.href = "/mainpage/mainpage.html";
  ```
- `common-api.js` 会自动读取并附加 `Authorization: Bearer ${token}`。

> 没有登录页也不影响本地 Mock 联调：Apifox 可在 Mock 中忽略 token 或返回默认 token。

---

## 7. 快速自测脚本（可拷贝到浏览器控制台）

```js
// 1) 创建术语库
const g = await ApiGlossary.create({
  title: "测试库",
  sourceLanguage: "中文",
  targetLanguage: "英文",
  description: "demo"
});

// 2) 添加术语
await ApiTerm.add({ glossaryId: g.data?.id || 1, sourceTerm: "螺蛳粉", targetTerm: "river snails rice noodle" });

// 3) 创建模板
const t = await ApiTemplate.create({ templateName: "科技文档模板" });

// 4) 翻译流程：先创建自动机拿 sessionId
const a = await ApiTranslate.createAutomaton({ glossaryIdList: [g.data?.id || 1] });
const sessionId = a.data?.sessionId;

// 5) 再做流式翻译（必须带模板）
const res = await ApiTranslate.customFlux({
  text: "螺蛳粉是广西一道悠久的美食",
  sessionId,
  templateId: t.data?.id || 1,
  selectedGlossaryCount: 1
});
console.log(res);
```

---

## 8. 验收清单

- [ ] `npm run dev` 可直接起服务，所有页面资源路径正确。
- [ ] 切换 `vite.config.mjs` 的 `TARGET` 到 Apifox Mock 地址，可离线联调所有接口。
- [ ] 术语库：创建 / 删除 / 编辑 / 列表 / 详情 全部可用。
- [ ] 术语：添加 / 删除 / **批量删除** / 编辑 / 查询 / **批量导入** 可用。
- [ ] 模板：创建 / 删除 / 编辑 / 列表 / 详情 可用。
- [ ] 翻译：
  - 创建自动机（必须先选术语库）能返回 `sessionId`；
  - **定制化翻译-流式**（必须带 `templateId`）能返回翻译结果。
- [ ] 页面中统一通过 `../common-api.js` 访问接口，后续切换后端仅需改代理，不改页面代码。

---

## 9. 常见问题（FAQ）

- **Q：为什么所有请求都走 `/api`？**  
  A：为了本地与线上统一入口，`/api` 由 Vite 代理到 Apifox Mock 或后端网关，页面代码零改动。

- **Q：路径老是 404？**  
  A：请确认各页面引入 `common-api.js` 的相对路径是否正确（大多数子目录只需 `../common-api.js`）。

- **Q：提示未携带 token？**  
  A：登录页把 token 存到 `localStorage.hxytt_token`；或者在 Mock 环境关闭鉴权/返回固定 token。

- **Q：批量导入术语用什么 Content-Type？**  
  A：`multipart/form-data`，`ApiTerm.addBatch(formData)` 已设置 `isForm: true`。

---

## 10. 后续接入真实微服务

- 把 `vite.config.mjs` 的 `TARGET` 指向微服务网关（如 `https://api.company.com`）。
- 如果网关有统一前缀（例如 `/gateway`），在 Vite 里把 `/api` 转发到 `/gateway` 即可，前端代码仍无需变更。

---

**至此，仓库应该已满足本地就能跑的要求**：有统一 `common-api.js`、接口与 Apifox 一致、翻译必须带模板、所有相对路径已说明。若还需把任何页面的交互钩到具体按钮/表单，告诉我文件名和按钮 `id`，可直接补上。
