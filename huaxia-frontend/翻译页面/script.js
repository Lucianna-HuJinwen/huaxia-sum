// 页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('hxytt_token');
    if (!token) {
        alert('请先登录后再访问此页面');
        window.location.href = '../login.html';
        return;
    }
});

// 全局变量
let selectedGlossaryIds = []; // 选中的术语库ID列表
let selectedTemplateId = null; // 选中的模板ID
let currentSessionId = null; // 当前会话ID

// 语言交换功能
const langExchangeBtn = document.getElementById('langExchangeBtn');
const srcLang = document.getElementById('srcLang');
const dstLang = document.getElementById('dstLang');

if (langExchangeBtn && srcLang && dstLang) {
    langExchangeBtn.addEventListener('click', function() {
        const temp = srcLang.textContent;
        srcLang.textContent = dstLang.textContent;
        dstLang.textContent = temp;
    });
}

// 术语库选择按钮功能
const terminologySelectBtn = document.getElementById('terminologySelectBtn');
const terminologySelectMenu = document.getElementById('terminologySelectMenu');

// 动态加载术语库列表
async function loadTerminologyList() {
    try {
        const glossaryList = await CommonApi.Glossary.list({ pageNum: 1, pageSize: 100 });
        console.log('术语库API响应:', glossaryList); // 调试日志
        
        if (glossaryList && glossaryList.rows) {
            const menu = document.getElementById('terminologySelectMenu');
            if (menu) {
                menu.innerHTML = ''; // 清空现有内容
                
                glossaryList.rows.forEach(glossary => {
                    console.log('处理术语库:', glossary); // 调试日志
                    const li = document.createElement('li');
                    li.className = 'terminology-item';
                    li.setAttribute('data-id', glossary.glossaryId);
                    
                    const a = document.createElement('a');
                    a.href = '#';
                    a.className = 'block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100';
                    a.textContent = glossary.title || `术语库${glossary.glossaryId}`;
                    
                    li.appendChild(a);
                    menu.appendChild(li);
                });
                
                // 添加确定按钮
                const confirmLi = document.createElement('li');
                confirmLi.className = 'border-t border-gray-200 my-1';
                const confirmBtn = document.createElement('button');
                confirmBtn.id = 'confirmTerminologyBtn';
                confirmBtn.className = 'w-full px-4 py-2 text-sm bg-primary text-white hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed';
                confirmBtn.textContent = '确定选择';
                confirmBtn.disabled = true;
                confirmLi.appendChild(confirmBtn);
                menu.appendChild(confirmLi);
                
                // 重新绑定事件
                bindTerminologyEvents();
                console.log('术语库列表加载完成，共', glossaryList.rows.length, '个术语库');
            }
        } else {
            console.warn('术语库列表为空或格式不正确:', glossaryList);
        }
    } catch (error) {
        console.error('加载术语库列表失败:', error);
    }
}

// 创建自动机功能
async function createAutomatonForSelectedGlossaries() {
    if (!selectedGlossaryIds || selectedGlossaryIds.length === 0) {
        console.warn('没有选中的术语库，无法创建自动机');
        return null;
    }
    
    try {
        console.log('开始创建自动机，术语库ID:', selectedGlossaryIds);
        
        // 显示加载状态
        showAutomatonLoadingState(true);
        
        // 保持ID为字符串格式，避免精度丢失
        console.log('术语库ID（字符串）:', selectedGlossaryIds);
        
        const automatonResult = await CommonApi.Translate.createAutomaton(selectedGlossaryIds);
        console.log('自动机创建API响应:', automatonResult); // 调试日志
        
        // 检查响应格式 - 后端返回的是R<T>格式
        if (automatonResult && automatonResult.code === 1000 && automatonResult.data) {
            currentSessionId = automatonResult.data;
            console.log('自动机创建成功，会话ID:', currentSessionId);
            
            // 显示成功状态
            showAutomatonSuccessState();
            return currentSessionId;
        } else {
            const errorMsg = automatonResult?.msg || '未知错误';
            throw new Error(`自动机创建失败：${errorMsg}`);
        }
        
    } catch (error) {
        console.error('创建自动机失败:', error);
        showAutomatonErrorState(error.message);
        return null;
    } finally {
        // 隐藏加载状态
        showAutomatonLoadingState(false);
    }
}

// 显示自动机加载状态
function showAutomatonLoadingState(show) {
    const terminologyBtn = document.getElementById('terminologySelectBtn');
    if (terminologyBtn) {
        if (show) {
            terminologyBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i>';
            terminologyBtn.title = '正在创建自动机...';
            terminologyBtn.disabled = true;
        } else {
            terminologyBtn.innerHTML = '<i class="fa fa-book"></i>';
            terminologyBtn.title = '术语库选择';
            terminologyBtn.disabled = false;
        }
    }
}

// 显示自动机创建成功状态
function showAutomatonSuccessState() {
    const terminologyBtn = document.getElementById('terminologySelectBtn');
    if (terminologyBtn) {
        terminologyBtn.innerHTML = '<i class="fa fa-check text-green-500"></i>';
        terminologyBtn.title = '自动机创建成功';
        
        // 2秒后恢复原状
        setTimeout(() => {
            terminologyBtn.innerHTML = '<i class="fa fa-book"></i>';
            terminologyBtn.title = '术语库选择';
        }, 2000);
    }
}

// 显示自动机创建失败状态
function showAutomatonErrorState(errorMessage) {
    const terminologyBtn = document.getElementById('terminologySelectBtn');
    if (terminologyBtn) {
        terminologyBtn.innerHTML = '<i class="fa fa-exclamation-triangle text-red-500"></i>';
        terminologyBtn.title = `自动机创建失败: ${errorMessage}`;
        
        // 3秒后恢复原状
        setTimeout(() => {
            terminologyBtn.innerHTML = '<i class="fa fa-book"></i>';
            terminologyBtn.title = '术语库选择';
        }, 3000);
    }
    
    // 显示错误提示
    alert(`自动机创建失败: ${errorMessage}`);
}

// 更新确定按钮状态
function updateConfirmButtonState() {
    const confirmBtn = document.getElementById('confirmTerminologyBtn');
    if (confirmBtn) {
        confirmBtn.disabled = selectedGlossaryIds.length === 0;
        confirmBtn.textContent = selectedGlossaryIds.length > 0 ? 
            `确定选择 (${selectedGlossaryIds.length})` : '确定选择';
    }
}

// 绑定术语库选择事件
function bindTerminologyEvents() {
    const terminologyItems = document.querySelectorAll('.terminology-item');
    const confirmBtn = document.getElementById('confirmTerminologyBtn');
    
    terminologyItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const dataId = item.getAttribute('data-id');
            if (!dataId) return;
            
            // 保持ID为字符串格式，避免精度丢失
            const glossaryId = dataId; // 保持字符串格式
            const a = item.querySelector('a');
            
            // 检查是否已选中
            const isSelected = selectedGlossaryIds.includes(glossaryId);
            
            if (isSelected) {
                // 取消选中
                selectedGlossaryIds = selectedGlossaryIds.filter(id => id !== glossaryId);
                a.classList.remove('text-primary');
                a.innerHTML = a.textContent;
            } else {
                // 添加选中（最多3个）
                if (selectedGlossaryIds.length >= 3) {
                    alert('最多只能选择3个术语库');
                    return;
                }
                selectedGlossaryIds.push(glossaryId);
                a.classList.add('text-primary');
                a.innerHTML = '<i class="fa fa-check mr-2"></i>' + a.textContent;
            }
            
            console.log('当前选中的术语库ID:', selectedGlossaryIds);
            updateConfirmButtonState();
        });
    });
    
    // 确定按钮点击事件
    if (confirmBtn) {
        confirmBtn.addEventListener('click', async function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            if (selectedGlossaryIds.length === 0) {
                alert('请先选择术语库');
                return;
            }
            
            // 关闭菜单
            const terminologySelectMenu = document.getElementById('terminologySelectMenu');
            if (terminologySelectMenu) {
                terminologySelectMenu.classList.add('hidden');
            }
            
            // 创建自动机
            await createAutomatonForSelectedGlossaries();
        });
    }
}

if (terminologySelectBtn && terminologySelectMenu) {
    terminologySelectBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        terminologySelectMenu.classList.toggle('hidden');
    });
    
    // 阻止下拉菜单内的点击事件冒泡
    terminologySelectMenu.addEventListener('click', function(e) {
        e.stopPropagation();
    });
    
    // 页面加载时加载术语库列表
    loadTerminologyList();
}

// 点击页面其他地方关闭术语库下拉菜单
document.addEventListener('click', function() {
    if (terminologySelectMenu && !terminologySelectMenu.classList.contains('hidden')) {
        terminologySelectMenu.classList.add('hidden');
    }
});

// 模板选择功能已禁用
/***** === AI 助手：/assistant/chat === *****/
(function(){
  const BASE = "/api";                 // 走 Vite 代理
  const PATH_AI = "/assistant/chat";   // 接口路径
  const TOKEN_KEY = "hxytt_token";     // 登录页存的 token
  const USER_KEY  = "hxytt_user";      // 如有保存用户对象，可从里取 id
  const USE_BEARER = false;            // 如果后端要 'Bearer xxx'，改为 true

  const list = document.getElementById("aiChatList");
  const input = document.getElementById("aiInput");
  const sendBtn = document.getElementById("aiSendBtn");
  const tip = document.getElementById("aiTip");

  if (!list || !input || !sendBtn) return; // 页面无该区域就跳过

  function scrollToBottom(){ list.scrollTop = list.scrollHeight; }
  function bubble({mine, text}) {
    const wrap = document.createElement("div");
    wrap.className = (mine ? "bg-gray-50" : "bg-primary/10") + " p-3 rounded-lg";
    wrap.innerHTML = `<p class="text-sm ${mine ? "text-gray-700" : "text-gray-800"}">${escapeHtml(text)}</p>`;
    list.appendChild(wrap);
    scrollToBottom();
  }
  function escapeHtml(s){ return String(s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }

  async function apiChat(message){
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) throw new Error("未登录：缺少 token");

    // 尝试取 userId（没有就 0）
    let userId = 0;
    try {
      const u = JSON.parse(localStorage.getItem(USER_KEY) || "null");
      if (u && typeof u.id === "number") userId = u.id;
    } catch {}

    const headers = { "Content-Type": "application/json" };
    headers["Authorization"] = USE_BEARER ? `Bearer ${token}` : token;

    const resp = await fetch(BASE + PATH_AI, {
      method: "POST",
      headers,
      body: JSON.stringify({ userId, message }),
    });
    const ct = resp.headers.get("content-type") || "";
    const data = ct.includes("application/json") ? await resp.json() : await resp.text();
    if (!resp.ok) throw new Error((data && (data.message || data.error)) || `HTTP ${resp.status}`);

    // 兼容多种返回结构 - 优先处理R<T>格式的响应
    const reply = data?.data ?? data?.reply ?? data?.answer ?? (typeof data === "string" ? data : "");
    return reply || ("（已请求成功，但无 data 字段）\n" + JSON.stringify(data, null, 2));
  }

  async function send(){
    const msg = (input.value || "").trim();
    if (!msg) return;

    bubble({mine:true, text: msg});
    input.value = "";
    tip.textContent = "思考中…";

    try{
      const ans = await apiChat(msg);
      bubble({mine:false, text: ans});
      tip.textContent = "";
    }catch(e){
      tip.textContent = "发送失败：" + (e.message || e);
      bubble({mine:false, text: "❌ " + (e.message || e)});
    }
  }

  sendBtn.addEventListener("click", send);
  input.addEventListener("keydown", (e)=>{
    if(e.key === "Enter" && !e.shiftKey){
      e.preventDefault();
      send();
    }
  });
})();

/***** === 翻译功能集成 === *****/
(function(){
    // DOM元素
    const btnTranslate = document.getElementById('btnTranslate');
    const srcText = document.getElementById('srcText');
    const dstText = document.getElementById('dstText');
    const charCount = document.querySelector('.text-sm.text-gray-500');

    if (!btnTranslate || !srcText || !dstText) return;

    // 清除现有的template缓存 - 在需要时清除可能过期的模板缓存
    function clearTemplateCache() {
        const selectedCategory = localStorage.getItem('selectedCategory');
        if (selectedCategory) {
            localStorage.removeItem(`templateId_${selectedCategory}`);
            console.log(`已清除板块 ${selectedCategory} 的模板缓存`);
        }
    }

    // 获取当前板块对应的templateId
    function getCurrentCategoryTemplateId() {
        const selectedCategory = localStorage.getItem('selectedCategory');
        if (selectedCategory) {
            const templateId = localStorage.getItem(`templateId_${selectedCategory}`);
            console.log(`当前板块 ${selectedCategory} 的模板ID:`, templateId);
            return templateId;
        }
        console.log('未找到选择的板块信息');
        return null;
    }

    // 重新加载模板功能
    async function reloadTemplate() {
        const selectedCategory = localStorage.getItem('selectedCategory');
        if (!selectedCategory) {
            alert('未找到选择的板块信息，请返回板块页面重新选择');
            return;
        }

        try {
            console.log(`开始重新加载板块 ${selectedCategory} 的模板...`);

            // 显示加载状态
            btnTranslate.disabled = true;
            btnTranslate.textContent = '重新加载模板中...';
            btnTranslate.classList.add('opacity-50');

            // 清除缓存（可能缓存已过期）
            clearTemplateCache();

            // 重新尝试获取templateId
            const templateId = getCurrentCategoryTemplateId();
            if (templateId) {
                console.log('重新获取模板ID成功:', templateId);
                selectedTemplateId = templateId;
                alert('模板已重新加载，请重新点击翻译按钮');
            } else {
                // 提示用户返回板块页面重新填写模板
                const confirmReload = confirm(
                    `检测到模板可能已过期或不存在。\n\n` +
                    `请返回"${selectedCategory}"板块页面，\n` +
                    `重新填写模板内容，然后点击"前往翻译"。\n\n` +
                    `是否现在返回？`
                );

                if (confirmReload) {
                    // 返回板块页面
                    const categoryUrlMap = {
                        '思想流派': '../思想流派/index.html',
                        '诗词歌赋': '../诗词歌赋/index.html',
                        '民俗音乐': '../民俗音乐/index.html',
                        '非遗传承': '../非遗传承/index.html',
                        '饮食文化': '../饮食文化/index.html',
                        '传统节日': '../传统节日/index.html'
                    };

                    const url = categoryUrlMap[selectedCategory];
                    if (url) {
                        window.location.href = url;
                    } else {
                        alert('板块页面地址未找到');
                    }
                }
            }

        } catch (error) {
            console.error('重新加载模板失败:', error);
            alert(`重新加载模板失败: ${error.message}`);
        } finally {
            // 恢复翻译按钮状态
            btnTranslate.disabled = false;
            btnTranslate.textContent = '翻译';
            btnTranslate.classList.remove('opacity-50');
        }
    }

    // 初始化：获取模板ID（不清除缓存）
    selectedTemplateId = getCurrentCategoryTemplateId();
    
    // 字符计数更新
    function updateCharCount() {
        const count = srcText.value.length;
        if (charCount) {
            charCount.textContent = `${count}/5000`;
            if (count > 5000) {
                charCount.classList.add('text-red-500');
                charCount.classList.remove('text-gray-500');
            } else {
                charCount.classList.remove('text-red-500');
                charCount.classList.add('text-gray-500');
            }
        }
    }
    
    // 监听输入框变化
    srcText.addEventListener('input', updateCharCount);
    
    // 翻译按钮点击事件
    btnTranslate.addEventListener('click', async function() {
        const text = srcText.value.trim();
        if (!text) {
            alert('请输入要翻译的文本');
            return;
        }
        
        if (text.length > 5000) {
            alert('文本长度不能超过5000字符');
            return;
        }
        
        // 禁用翻译按钮
        btnTranslate.disabled = true;
        btnTranslate.textContent = '翻译中...';
        btnTranslate.classList.add('opacity-50');
        
        try {
            // 1. 如果没有会话ID但有选中的术语库，先创建自动机
            if (!currentSessionId && selectedGlossaryIds.length > 0) {
                console.log('翻译时创建自动机，术语库ID:', selectedGlossaryIds);
                
                // 保持ID为字符串格式，避免精度丢失
                console.log('翻译时术语库ID（字符串）:', selectedGlossaryIds);
                
                const automatonResult = await CommonApi.Translate.createAutomaton(selectedGlossaryIds);
                console.log('翻译时自动机创建API响应:', automatonResult); // 调试日志
                
                if (automatonResult && automatonResult.code === 1000 && automatonResult.data) {
                    currentSessionId = automatonResult.data;
                    console.log('自动机创建成功，会话ID:', currentSessionId);
                }
            }
            
            // 2. 调用翻译API
            const translateData = {
                text: text,
                sessionId: currentSessionId,
                templateId: selectedTemplateId,
                selectedGlossaryCount: selectedGlossaryIds.length,
                srcLang: srcLang.textContent,
                dstLang: dstLang.textContent
            };

            console.log('调用翻译API，参数:', translateData);
            console.log('当前使用的模板ID:', selectedTemplateId);
            if (selectedTemplateId) {
                const selectedCategory = localStorage.getItem('selectedCategory');
                console.log(`模板来自板块: ${selectedCategory}`);
            } else {
                console.warn('未找到模板ID，清除缓存并准备重新加载模板...');
                // 清除缓存（可能缓存已过期）
                clearTemplateCache();
                // 自动尝试重新加载模板
                await reloadTemplate();
                return; // 阻止当前翻译请求
            }
            
            // 使用普通翻译接口
            const token = window.CommonAuth ? window.CommonAuth.getToken() : localStorage.getItem('hxytt_token');
            
            const response = await fetch('/api/translate/custom', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? (token.startsWith('Bearer ') ? token : `Bearer ${token}`) : ''
                },
                body: JSON.stringify(translateData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.text();
            dstText.value = result;
            
        } catch (error) {
            console.error('翻译失败:', error);
            dstText.value = `翻译失败: ${error.message}`;
        } finally {
            // 恢复翻译按钮
            btnTranslate.disabled = false;
            btnTranslate.textContent = '翻译';
            btnTranslate.classList.remove('opacity-50');
        }
    });
    
    // 模板选择功能已禁用
    
    // 清除按钮功能
    const clearBtn = document.querySelector('button[title="一键清除"]');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            srcText.value = '';
            dstText.value = '';
            updateCharCount();
        });
    }
    
    // 复制按钮功能
    const copyBtn = document.querySelector('button[title="复制文本"]');
    if (copyBtn) {
        copyBtn.addEventListener('click', function() {
            if (dstText.value) {
                navigator.clipboard.writeText(dstText.value).then(() => {
                    // 可以添加一个提示
                    console.log('翻译结果已复制到剪贴板');
                }).catch(err => {
                    console.error('复制失败:', err);
                });
            }
        });
    }
    
    // 初始化字符计数
    updateCharCount();
    
})();