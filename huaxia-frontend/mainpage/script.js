// 为所有探索按钮添加点击事件监听器
document.addEventListener('DOMContentLoaded', function() {
    // 页面加载时检查登录状态
    const token = localStorage.getItem('hxytt_token');
    if (!token) {
        alert('请先登录后再访问此页面');
        window.location.href = 'login.html';
        return;
    }
    // 获取所有的探索按钮
    const exploreButtons = document.querySelectorAll('.category-card button');
    
    // 为每个按钮添加点击事件
    exploreButtons.forEach((button, index) => {
        button.addEventListener('click', function() {
            // 获取板块名称
            const categoryName = ['思想流派', '诗词歌赋', '民俗音乐', '非遗传承', '饮食文化', '传统节日'][index];

            // 存储当前选择的板块信息到localStorage
            localStorage.setItem('selectedCategory', categoryName);
            console.log('用户选择了板块:', categoryName);

            // 根据板块名称跳转到对应的网页
            const urlMap = {
                '思想流派': '思想流派/index.html',
                '诗词歌赋': '诗词歌赋/index.html',
                '民俗音乐': '民俗音乐/index.html',
                '非遗传承': '非遗传承/index.html',
                '饮食文化': '饮食文化/index.html',
                '传统节日': '传统节日/index.html'
            };

            // 打开对应板块的网页
            if (urlMap[categoryName]) {
                window.open(urlMap[categoryName], '_blank');
            } else {
                // 如果没有对应板块，显示提示
                alert('该板块页面正在开发中');
            }
        });
    });

    // 顶部导航栏下拉菜单功能
    // 获取菜单元素
    const languageAssetsBtn = document.getElementById('languageAssetsBtn');
    const languageAssetsDropdown = document.getElementById('languageAssetsDropdown');
    const userBtn = document.getElementById('userBtn');
    const userDropdown = document.getElementById('userDropdown');

    // 语言资产下拉菜单切换
    if (languageAssetsBtn && languageAssetsDropdown) {
        languageAssetsBtn.addEventListener('click', function(event) {
            // 阻止事件冒泡，避免触发document的点击事件
            event.stopPropagation();
            
            // 切换下拉菜单显示状态
            languageAssetsDropdown.classList.toggle('hidden');
            
            // 如果用户菜单是打开的，则关闭它
            if (userDropdown && !userDropdown.classList.contains('hidden')) {
                userDropdown.classList.add('hidden');
            }
        });
    }

    // 个人头像下拉菜单切换
    if (userBtn && userDropdown) {
        userBtn.addEventListener('click', function(event) {
            // 阻止事件冒泡，避免触发document的点击事件
            event.stopPropagation();
            
            // 切换下拉菜单显示状态
            userDropdown.classList.toggle('hidden');
            
            // 如果语言资产菜单是打开的，则关闭它
            if (languageAssetsDropdown && !languageAssetsDropdown.classList.contains('hidden')) {
                languageAssetsDropdown.classList.add('hidden');
            }
        });
    }

    // 点击页面其他区域时关闭所有下拉菜单
    document.addEventListener('click', function() {
        if (languageAssetsDropdown && !languageAssetsDropdown.classList.contains('hidden')) {
            languageAssetsDropdown.classList.add('hidden');
        }
        if (userDropdown && !userDropdown.classList.contains('hidden')) {
            userDropdown.classList.add('hidden');
        }
    });

    // 防止点击下拉菜单内部时关闭菜单
    const dropdowns = [languageAssetsDropdown, userDropdown];
    dropdowns.forEach(dropdown => {
        if (dropdown) {
            dropdown.addEventListener('click', function(event) {
                event.stopPropagation();
            });
        }
    });

    // AI助手功能
    initAIAssistant();
});

// AI助手初始化函数
function initAIAssistant() {
    const aiAssistantBtn = document.getElementById('aiAssistantBtn');
    const aiAssistantDrawer = document.getElementById('aiAssistantDrawer');
    const aiAssistantOverlay = document.getElementById('aiAssistantOverlay');
    const aiAssistantClose = document.getElementById('aiAssistantClose');
    const aiChatList = document.getElementById('aiChatList');
    const aiInput = document.getElementById('aiInput');
    const aiSendBtn = document.getElementById('aiSendBtn');
    const aiTip = document.getElementById('aiTip');

    if (!aiAssistantBtn || !aiAssistantDrawer) return;

    // 打开AI助手抽屉
    function openDrawer() {
        aiAssistantDrawer.classList.add('open');
        aiAssistantOverlay.classList.add('show');
        setTimeout(() => aiInput.focus(), 300);
    }

    // 关闭AI助手抽屉
    function closeDrawer() {
        aiAssistantDrawer.classList.remove('open');
        aiAssistantOverlay.classList.remove('show');
    }

    // 点击按钮打开抽屉
    aiAssistantBtn.addEventListener('click', openDrawer);

    // 点击关闭按钮
    aiAssistantClose.addEventListener('click', closeDrawer);

    // 点击遮罩层关闭抽屉
    aiAssistantOverlay.addEventListener('click', closeDrawer);

    // AI助手聊天功能
    if (aiChatList && aiInput && aiSendBtn && aiTip) {
        // 滚动到底部
        function scrollToBottom() {
            aiChatList.scrollTop = aiChatList.scrollHeight;
        }

        // 创建消息气泡
        function createBubble({mine, text}) {
            const wrap = document.createElement("div");
            wrap.className = (mine ? "bg-gray-100" : "bg-blue-50") + " p-3 rounded-lg ai-message";
            wrap.innerHTML = `<p class="text-sm ${mine ? "text-gray-700" : "text-gray-800"}">${escapeHtml(text)}</p>`;
            aiChatList.appendChild(wrap);
            scrollToBottom();
        }

        // HTML转义
        function escapeHtml(s) {
            return String(s).replace(/[&<>"']/g, m => ({
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#39;'
            }[m]));
        }

        // API流式聊天请求
        async function apiChatStream(message, onChunk) {
            const token = localStorage.getItem('hxytt_token');
            if (!token) throw new Error("未登录：缺少 token");

            // 尝试取 userId（没有就 0）
            let userId = 0;
            try {
                const u = JSON.parse(localStorage.getItem('hxytt_user') || "null");
                if (u && typeof u.id === "number") userId = u.id;
            } catch {}

            const headers = { "Content-Type": "application/json" };
            headers["Authorization"] = token;

            const resp = await fetch(window.API_BASE + '/assistant/chat/stream', {
                method: "POST",
                headers,
                body: JSON.stringify({ userId, message }),
            });

            if (!resp.ok) {
                const errorText = await resp.text();
                throw new Error(`HTTP ${resp.status}: ${errorText}`);
            }

            // 使用 ReadableStream 读取流式响应
            const reader = resp.body.getReader();
            const decoder = new TextDecoder('utf-8');
            
            try {
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;
                    
                    const chunk = decoder.decode(value, { stream: true });
                    if (chunk) {
                        onChunk(chunk);
                    }
                }
            } finally {
                reader.releaseLock();
            }
        }

        // 发送消息
        async function sendMessage() {
            const msg = (aiInput.value || "").trim();
            if (!msg) return;

            createBubble({mine: true, text: msg});
            aiInput.value = "";
            aiTip.textContent = "AI正在思考中…";

            // 创建AI回复气泡（空内容）
            const aiBubble = document.createElement("div");
            aiBubble.className = "bg-blue-50 p-3 rounded-lg ai-message";
            const aiText = document.createElement("p");
            aiText.className = "text-sm text-gray-800";
            aiBubble.appendChild(aiText);
            aiChatList.appendChild(aiBubble);

            let fullResponse = "";

            try {
                await apiChatStream(msg, (chunk) => {
                    fullResponse += chunk;
                    aiText.textContent = fullResponse;
                    scrollToBottom();
                });
                aiTip.textContent = "";
            } catch (e) {
                aiTip.textContent = "发送失败：" + (e.message || e);
                aiText.textContent = "❌ " + (e.message || e);
            }
        }

        // 发送按钮点击事件
        aiSendBtn.addEventListener("click", sendMessage);

        // 回车发送消息
        aiInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }
}