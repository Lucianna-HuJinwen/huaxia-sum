document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('hxytt_token')) {
        console.warn('【测试模式】自动注入Mock Token');
        localStorage.setItem('hxytt_token', 'mock-token-for-test');
    }

    initNavigation();
    initToolbarActions();
    initTranslation();
    initAIAssistant();
});

// ================== A. 导航 (功能保留) ==================
function initNavigation() {
    const backBtn = document.getElementById('backBtn');
    const categoryMap = {
        '传统节日': '../传统节日/index.html',
        '思想流派': '../思想流派/index.html',
        '诗词歌赋': '../诗词歌赋/index.html',
        '民俗音乐': '../民俗音乐/index.html',
        '非遗传承': '../非遗传承/index.html',
        '饮食文化': '../饮食文化/index.html'
    };

    if (backBtn) {
        backBtn.addEventListener('click', () => {
            const currentCategory = localStorage.getItem('selectedCategory');
            if (currentCategory && categoryMap[currentCategory]) {
                window.location.href = categoryMap[currentCategory];
            } else {
                if(confirm('未找到来源板块，是否返回系统首页？')) window.location.href = '../index.html'; 
            }
        });
    }
}

// ================== B. 工具栏 (导入导出修复+保留) ==================
function initToolbarActions() {
    const srcText = document.getElementById('srcText');
    const dstText = document.getElementById('dstText');

    const btnImport = document.getElementById('btnImportDoc');
    const importInput = document.getElementById('importFileInput');
    
    if (btnImport && importInput) {
        btnImport.addEventListener('click', () => importInput.click());
        importInput.addEventListener('change', async (e) => {
            const file = e.target.files[0];
            if (!file) return;
            
            const fileName = file.name.toLowerCase();
            let extractedText = "";

            try {
                if (fileName.endsWith('.docx')) {
                    if (typeof mammoth === 'undefined') throw new Error('解析库未加载');
                    const arrayBuffer = await file.arrayBuffer();
                    const result = await mammoth.extractRawText({ arrayBuffer: arrayBuffer });
                    extractedText = result.value;
                } else if (fileName.endsWith('.pdf')) {
                    if (typeof pdfjsLib === 'undefined') throw new Error('PDF库未加载');
                    const arrayBuffer = await file.arrayBuffer();
                    const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
                    let fullText = "";
                    for (let i = 1; i <= pdf.numPages; i++) {
                        const page = await pdf.getPage(i);
                        const textContent = await page.getTextContent();
                        const pageText = textContent.items.map(item => item.str).join(' ');
                        fullText += pageText + "\n";
                    }
                    extractedText = fullText;
                } else {
                    extractedText = await file.text();
                }
                srcText.innerText = extractedText;
                srcText.dispatchEvent(new Event('input'));
            } catch (error) {
                alert('导入失败: ' + error.message);
            }
            e.target.value = '';
        });
    }

    // 导出 Word
    document.getElementById('exportWord')?.addEventListener('click', (e) => {
        e.preventDefault();
        const content = dstText.innerHTML;
        const html = `<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word'><head><meta charset='utf-8'></head><body>${content}</body></html>`;
        const blob = new Blob([html], { type: 'application/msword' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `译文_${new Date().toISOString().slice(0,10)}.doc`;
        a.click();
    });

    // 导出 PDF
    document.getElementById('exportPDF')?.addEventListener('click', (e) => {
        e.preventDefault();
        window.print();
    });

    //导出功能整体按钮
    const btn = document.getElementById("exportBtn");
    const menu = document.getElementById("exportMenu");
    const wrapper = document.getElementById("exportWrapper");

    // 点击按钮 → 展开/收起
    btn.addEventListener("click", (e) => {
    e.stopPropagation();
    menu.classList.toggle("hidden");
    });

    // 点击菜单内部不会关闭
    menu.addEventListener("click", (e) => e.stopPropagation());

    // 点击外部 → 收起
    document.addEventListener("click", () => {
     menu.classList.add("hidden");
    });


    // 一键清除
    document.getElementById('btnClearAll')?.addEventListener('click', () => {
        if(confirm('确定要清空所有内容吗？')) {
            srcText.innerHTML = '';
            dstText.innerHTML = '';
            srcText.dispatchEvent(new Event('input'));
        }
    });

    // 格式化
    document.getElementById('colorPicker')?.addEventListener('input', (e) => document.execCommand('foreColor', false, e.target.value));
    document.getElementById('fontSizeSelect')?.addEventListener('change', (e) => document.execCommand('fontSize', false, e.target.value));
    document.getElementById('btnUndo')?.addEventListener('click', () => document.execCommand('undo'));
    document.getElementById('btnRedo')?.addEventListener('click', () => document.execCommand('redo'));
    document.getElementById('btnCopy')?.addEventListener('click', () => {
        if (dstText.innerText) navigator.clipboard.writeText(dstText.innerText).then(() => alert('已复制'));
    });

    // 收藏
    document.getElementById('btnFavorite')?.addEventListener('click', () => {
        const src = srcText.innerText.trim();
        const dst = dstText.innerText.trim();
        if(src && dst && confirm('收藏当前结果？')) {
            const list = JSON.parse(localStorage.getItem('hxytt_fav_v2') || '[]');
            list.unshift({ src, dst, time: new Date().toLocaleString() });
            localStorage.setItem('hxytt_fav_v2', JSON.stringify(list));
        }
        renderList('hxytt_fav_v2', 'favoriteList');
        document.getElementById('favoriteModal').classList.remove('hidden');
    });

   // === 【核心修复】项目任务保存逻辑 ===
    const currentTaskId = localStorage.getItem('hxytt_current_task_id');
    const currentProjId = localStorage.getItem('hxytt_current_project_id');
    const btnSaveProject = document.getElementById('btnSaveToProject');

    // 只要有任务ID，就尝试回填数据
    if (currentTaskId && currentProjId) {
        if(btnSaveProject) {
            btnSaveProject.classList.remove('hidden');
            
            try {
                const allProjs = JSON.parse(localStorage.getItem('hxytt_projects') || '[]');
                // 注意：ID可能是数字或字符串，用 == 比较更稳妥
                const proj = allProjs.find(p => p.id == currentProjId);
                const task = proj?.tasks.find(t => t.id == currentTaskId);
                
                if(task) {
                    // 1. 无论有没有译文，都要回填原文！
                    if(task.content) srcText.innerText = task.content; 
                    // 2. 如果有旧译文，也回填
                    if(task.translation) dstText.innerHTML = task.translation; 
                    
                    // 触发一下字数统计
                    srcText.dispatchEvent(new Event('input'));
                }
            } catch(e) {
                console.error("回填项目数据失败:", e);
            }

            // 绑定保存按钮事件
            btnSaveProject.addEventListener('click', () => {
                const translation = dstText.innerHTML; 
                let allProjects = JSON.parse(localStorage.getItem('hxytt_projects') || '[]');
                let project = allProjects.find(p => p.id == currentProjId);
                
                if (project) {
                    let taskIndex = project.tasks.findIndex(t => t.id == currentTaskId);
                    if (taskIndex > -1) {
                        project.tasks[taskIndex].translation = translation;
                        // 自动把状态更为"翻译中"或"待审校"
                        // project.tasks[taskIndex].status = 'review'; 
                        
                        localStorage.setItem('hxytt_projects', JSON.stringify(allProjects));
                        
                        // 清理标记
                        localStorage.removeItem('hxytt_current_task_id');
                        localStorage.removeItem('hxytt_current_project_id');
                        
                        alert('保存成功！正在返回项目空间...');
                        
                        // 【关键修复】返回时带上参数，告诉它要打开哪个项目
                        window.location.href = `../project_management/index.html?activeProject=${currentProjId}`;
                    }
                }
            });
        }
    }
    initHistoryAndFavorite();
}

// ================== C. 翻译逻辑 (功能保留) ==================
function initTranslation() {
    const btnTranslate = document.getElementById('btnTranslate');
    const srcText = document.getElementById('srcText');
    const dstText = document.getElementById('dstText');
    const charCount = document.getElementById('charCount');

    srcText.addEventListener('input', () => {
        const c = srcText.innerText.replace(/\n/g,'').length;
        charCount.innerText = `${c}/5000`;
    });

    if (btnTranslate) {
        btnTranslate.addEventListener('click', async () => {
            const text = srcText.innerText.trim();
            if(!text) return alert('请输入内容');
            
            btnTranslate.disabled = true;
            btnTranslate.innerHTML = '<i class="fa fa-spinner fa-spin"></i>';

            try {
                const payload = { text, sessionId: null, templateId: null };
                const res = await fetch('/api/translate/custom', {
                    method: 'POST', 
                    headers: {'Content-Type': 'application/json', 'Authorization': localStorage.getItem('hxytt_token')},
                    body: JSON.stringify(payload)
                });
                if(!res.ok) throw new Error('API Error');
                dstText.innerText = await res.text();
            } catch (e) {
                console.warn('使用模拟数据');
                await new Promise(r => setTimeout(r, 800));
                const mock = `【模拟译文】\n\n原文：${text}\n\n[测试成功]：Word/PDF导入已修复，右侧AI助手支持流式Markdown。`;
                dstText.innerText = mock;
                saveToHistory(text, mock);
            } finally {
                btnTranslate.disabled = false;
                btnTranslate.textContent = '翻译';
            }
        });
    }
}

// ================== D. 历史/收藏 (功能保留) ==================
function initHistoryAndFavorite() {
    const KEY_HIST = 'hxytt_history_v2';
    
    window.saveToHistory = (src, dst) => {
        const list = JSON.parse(localStorage.getItem(KEY_HIST)||'[]');
        list.unshift({src, dst, time: new Date().toLocaleString()});
        localStorage.setItem(KEY_HIST, JSON.stringify(list));
    };

    window.renderList = (key, id) => {
        const list = JSON.parse(localStorage.getItem(key)||'[]');
        const div = document.getElementById(id);
        div.innerHTML = list.length ? '' : '<div class="text-center text-gray-400 py-4">无数据</div>';
        list.forEach((item, idx) => {
            const el = document.createElement('div');
            el.className = "bg-white p-2 border rounded hover:bg-gray-50 cursor-pointer text-sm mb-2";
            el.innerHTML = `<div class="flex justify-between text-xs text-gray-400 mb-1"><span>${item.time}</span><span class="text-red-500 hover:underline del-btn">删除</span></div><div class="truncate text-gray-600">${item.src}</div>`;
            el.addEventListener('click', (e) => {
                if(e.target.classList.contains('del-btn')) {
                    list.splice(idx, 1);
                    localStorage.setItem(key, JSON.stringify(list));
                    renderList(key, id);
                } else {
                    document.getElementById('srcText').innerText = item.src;
                    document.getElementById('dstText').innerText = item.dst;
                    document.getElementById('historyModal').classList.add('hidden');
                    document.getElementById('favoriteModal').classList.add('hidden');
                }
            });
            div.appendChild(el);
        });
    };

    document.getElementById('btnHistory')?.addEventListener('click', () => {
        renderList(KEY_HIST, 'historyList');
        document.getElementById('historyModal').classList.remove('hidden');
    });
    document.getElementById('clearHistoryBtn')?.addEventListener('click', () => {
        localStorage.removeItem(KEY_HIST);
        renderList(KEY_HIST, 'historyList');
    });
}

// ================== E. AI助手 (Markdown + 流式) ==================
function initAIAssistant() {
    const btn = document.getElementById('aiSendBtn');
    const input = document.getElementById('aiInput');
    const list = document.getElementById('aiChatList');
    
    if(btn) {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); btn.click(); }
        });

        btn.addEventListener('click', async () => {
            const msg = input.value.trim();
            if(!msg) return;

            // 1. 用户消息
            const userDiv = document.createElement('div');
            userDiv.className = 'bg-gray-100 p-3 rounded-lg ml-auto max-w-[85%] mb-3 text-sm shadow-sm';
            userDiv.textContent = msg;
            list.appendChild(userDiv);
            input.value = '';
            list.scrollTop = list.scrollHeight;

            // 2. AI 思考中
            const aiDiv = document.createElement('div');
            aiDiv.className = 'bg-white border border-gray-200 p-3 rounded-lg mr-auto max-w-[85%] mb-3 text-sm shadow-sm markdown-body';
            aiDiv.innerHTML = '<div class="flex items-center text-gray-400"><i class="fa fa-spinner fa-spin mr-2"></i>思考中...</div>';
            list.appendChild(aiDiv);
            list.scrollTop = list.scrollHeight;

            try {
                // 3. 尝试调用流式接口 (后端对接点)
                const token = localStorage.getItem('hxytt_token');
                const response = await fetch('/api/assistant/chat/stream', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': token },
                    body: JSON.stringify({ message: msg })
                });

                if (response.ok) {
                    // 4. 处理流
                    const reader = response.body.getReader();
                    const decoder = new TextDecoder();
                    let fullText = '';
                    while (true) {
                        const { done, value } = await reader.read();
                        if (done) break;
                        fullText += decoder.decode(value, { stream: true });
                        aiDiv.innerHTML = marked.parse(fullText); // 实时渲染Markdown
                        list.scrollTop = list.scrollHeight;
                    }
                } else {
                    throw new Error('API Error');
                }

            } catch (error) {
                // 5. 模拟流式 + Markdown (测试用)
                console.warn('模拟流式Markdown输出');
                const mockText = `这是 **Markdown 流式输出** 演示。\n\n- 支持 **加粗**\n- 支持 [链接](#)\n- 代码块：\n\`\`\`javascript\nconsole.log("Hello");\n\`\`\`\n\n请联系后端开发对接 \`/api/assistant/chat/stream\` 接口。`;
                let current = '';
                for (let i = 0; i < mockText.length; i++) {
                    await new Promise(r => setTimeout(r, 30));
                    current += mockText[i];
                    aiDiv.innerHTML = marked.parse(current);
                    list.scrollTop = list.scrollHeight;
                }
            }
        });
    }
}