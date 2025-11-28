// 全局状态模拟
let projects = JSON.parse(localStorage.getItem('hxytt_projects') || '[]');
let currentProject = null;
let currentUserRole = 'manager'; // 'manager' 或 'translator'

document.addEventListener('DOMContentLoaded', () => {
    renderProjectList();
    
    // 【新增】检查是否有这就需要自动打开的项目
    const urlParams = new URLSearchParams(window.location.search);
    const activeId = urlParams.get('activeProject');
    if (activeId) {
        // 稍微延迟一下，确保列表渲染完毕，体验更好
        setTimeout(() => selectProject(Number(activeId)), 100);
    }
});


// === 1. 项目管理逻辑 ===

function createProject() {
    const name = document.getElementById('newProjName').value;
    const glossary = document.getElementById('newProjGlossary').value; // 语料库ID
    const glossaryName = document.getElementById('newProjGlossary').selectedOptions[0].text;
    const date = document.getElementById('newProjDate').value;

    if (!name || !date) return alert('请补全信息');

    const newProj = {
        id: Date.now(),
        name: name,
        glossaryId: glossary,
        glossaryName: glossaryName, // 需求1: 共享语料库
        deadline: date,
        tasks: []
    };

    projects.unshift(newProj);
    saveData();
    renderProjectList();
    closeModal('createProjectModal');
    selectProject(newProj.id); // 自动选中
}

function renderProjectList() {
    const list = document.getElementById('projectList');
    list.innerHTML = '';
    
    projects.forEach(p => {
        const div = document.createElement('div');
        div.className = `p-3 rounded cursor-pointer hover:bg-gray-100 transition-colors ${currentProject?.id === p.id ? 'bg-red-50 border-l-4 border-primary' : ''}`;
        div.onclick = () => selectProject(p.id);
        div.innerHTML = `
            <div class="font-bold text-gray-800 text-sm truncate">${p.name}</div>
            <div class="text-xs text-gray-500 mt-1 flex justify-between">
                <span><i class="fa fa-book"></i> ${p.glossaryId ? '已挂载语料' : '无语料'}</span>
                <span>${p.tasks.length} 任务</span>
            </div>
        `;
        list.appendChild(div);
    });
}

function selectProject(id) {
    currentProject = projects.find(p => p.id === id);
    renderProjectList(); // 更新高亮
    
    // 显示详情页
    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('projectDetail').classList.remove('hidden');
    
    // 填充头部信息
    document.getElementById('detailTitle').innerText = currentProject.name;
    document.getElementById('detailDeadline').innerText = currentProject.deadline;
    document.getElementById('detailGlossary').innerText = currentProject.glossaryName;
    
    renderTasks();
    renderChat();
}

// === 2. 任务管理逻辑 (核心特色) ===

function renderTasks() {
    // 清空四个泳道
    ['colTodo', 'colDoing', 'colReview', 'colDone'].forEach(id => document.getElementById(id).innerHTML = '');
    
    let total = currentProject.tasks.length;
    let done = 0;

    currentProject.tasks.forEach(task => {
        const card = createTaskCard(task);
        
        if (task.status === 'todo') document.getElementById('colTodo').appendChild(card);
        else if (task.status === 'doing') document.getElementById('colDoing').appendChild(card);
        else if (task.status === 'review') document.getElementById('colReview').appendChild(card);
        else if (task.status === 'done') {
            document.getElementById('colDone').appendChild(card);
            done++;
        }
    });

    // 更新进度条
    const pct = total === 0 ? 0 : Math.round((done / total) * 100);
    document.getElementById('detailProgress').style.width = `${pct}%`;
    document.getElementById('detailProgressText').innerText = `${pct}%`;
}

function createTaskCard(task) {
    const div = document.createElement('div');
    div.className = 'task-card animate-fade-in group';
    div.innerHTML = `
        <div class="flex justify-between items-start mb-2">
            <span class="font-bold text-sm text-gray-800">${task.name}</span>
            <span class="tag ${getStatusColor(task.status)}">${getStatusText(task.status)}</span>
        </div>
        <div class="text-xs text-gray-500 mb-2 truncate">${task.content}</div>
        <div class="flex justify-between items-center border-t pt-2 mt-2">
            <div class="flex items-center text-xs text-gray-400">
                <div class="w-5 h-5 rounded-full bg-gray-200 flex items-center justify-center mr-1 text-[10px]">${task.assignee[0]}</div>
                ${task.assignee}
            </div>
            ${getActionButton(task)}
        </div>
    `;
    return div;
}

function getStatusColor(status) {
    if(status==='todo') return 'bg-gray-100 text-gray-600';
    if(status==='doing') return 'bg-blue-100 text-blue-600';
    if(status==='review') return 'bg-yellow-100 text-yellow-600';
    return 'bg-green-100 text-green-600';
}

function getStatusText(s) {
    const map = {todo:'待处理', doing:'翻译中', review:'待审校', done:'已定稿'};
    return map[s] || s;
}

function getActionButton(task) {
    // 需求2 & 3: 平台内流转，无需退出
    if (task.status === 'todo') {
        return `<button onclick="updateTaskStatus(${task.id}, 'doing')" class="text-xs text-blue-500 hover:underline">开始翻译</button>`;
    } else if (task.status === 'doing') {
        // 这里模拟跳转到翻译页面，或者直接提交
        // 实际对接：可以 window.open(`../翻译页面/translation_page.html?taskId=${task.id}`)
        return `<div class="space-x-2">
            <button onclick="goToTransPage(${task.id})" class="text-xs text-primary hover:underline">去翻译</button>
            <button onclick="updateTaskStatus(${task.id}, 'review')" class="text-xs text-green-500 hover:underline">提交</button>
        </div>`;
    } else if (task.status === 'review') {
        return `<button onclick="openReviewModal(${task.id})" class="text-xs text-yellow-600 hover:underline">审校/整合</button>`;
    }
    return `<i class="fa fa-check text-green-500"></i>`;
}

// === 3. 操作与交互 ===

function addTask() {
    const name = document.getElementById('newTaskName').value;
    const content = document.getElementById('newTaskContent').value;
    const assignee = document.getElementById('newTaskAssignee').value;

    if(!name || !content) return alert('请填写任务信息');

    currentProject.tasks.push({
        id: Date.now(),
        name, content, assignee,
        status: 'todo',
        translation: '' // 暂存译文
    });
    
    saveData();
    renderTasks();
    closeModal('addTaskModal');
}

function updateTaskStatus(taskId, status) {
    const task = currentProject.tasks.find(t => t.id === taskId);
    if(task) {
        task.status = status;
        // 如果是提交，模拟生成一段译文
        if(status === 'review' && !task.translation) {
            task.translation = `[模拟译文] ${task.content} (已由${task.assignee}基于${currentProject.glossaryName}完成翻译)`;
        }
        saveData();
        renderTasks();
    }
}

function goToTransPage(taskId) {
    // 1. 锁定当前任务
    const task = currentProject.tasks.find(t => t.id === taskId);
    if (!task) return;

    // 2. 存入"当前操作任务ID"，供翻译页面读取
    localStorage.setItem('hxytt_current_task_id', taskId);
    localStorage.setItem('hxytt_current_project_id', currentProject.id);

    // 3. (可选) 如果任务已有暂存译文，也可以带过去，但通常翻译页会自己处理
    // 这里我们简单跳转
    if(confirm(`即将前往翻译工作台处理任务：${task.name}`)) {
        window.location.href = '../翻译页面/translation_page.html';
    }
}

function openReviewModal(taskId) {
    const task = currentProject.tasks.find(t => t.id === taskId);
    if(!task) return;

    document.getElementById('reviewModal').classList.remove('hidden');
    document.getElementById('reviewSrc').innerText = task.content;
    document.getElementById('reviewDst').value = task.translation;
    document.getElementById('reviewStatus').innerText = getStatusText(task.status);

    // 审校按钮
    const actionDiv = document.getElementById('reviewActions');
    actionDiv.innerHTML = `
        <button onclick="saveReview(${taskId})" class="px-3 py-1 bg-blue-500 text-white rounded text-sm">保存修改</button>
        <button onclick="approveTask(${taskId})" class="px-3 py-1 bg-green-500 text-white rounded text-sm">通过定稿</button>
        <button onclick="rejectTask(${taskId})" class="px-3 py-1 bg-red-100 text-red-500 rounded text-sm">驳回重译</button>
    `;
}

function saveReview(taskId) {
    const task = currentProject.tasks.find(t => t.id === taskId);
    task.translation = document.getElementById('reviewDst').value;
    saveData();
    alert('修改已保存');
}

function approveTask(taskId) {
    updateTaskStatus(taskId, 'done');
    closeModal('reviewModal');
}

function rejectTask(taskId) {
    updateTaskStatus(taskId, 'doing'); // 打回重做
    closeModal('reviewModal');
}

// 需求3: 整合导出
document.getElementById('btnExportAll').addEventListener('click', () => {
    const doneTasks = currentProject.tasks.filter(t => t.status === 'done');
    if(doneTasks.length === 0) return alert('当前没有已定稿的任务，无法整合。');
    
    let content = `项目：${currentProject.name}\n整合时间：${new Date().toLocaleString()}\n\n`;
    doneTasks.forEach(t => {
        content += `### ${t.name}\n原文：${t.content}\n译文：${t.translation}\n\n-------------------\n\n`;
    });

    // 模拟下载
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${currentProject.name}_整合稿.txt`;
    a.click();
});

// === 工具函数 ===
function saveData() {
    localStorage.setItem('hxytt_projects', JSON.stringify(projects));
}

function switchRole() {
    currentUserRole = currentUserRole === 'manager' ? 'translator' : 'manager';
    document.getElementById('currentRole').innerText = currentUserRole === 'manager' ? '项目经理' : '译员';
    
    // 简单控制权限显隐
    const managerBtns = document.querySelectorAll('.manager-only');
    managerBtns.forEach(b => b.style.display = currentUserRole === 'manager' ? 'inline-block' : 'none');
    
    alert(`已切换视角为：${document.getElementById('currentRole').innerText}`);
}

window.openCreateModal = () => document.getElementById('createProjectModal').classList.remove('hidden');
window.openAddTaskModal = () => document.getElementById('addTaskModal').classList.remove('hidden');
window.closeModal = (id) => document.getElementById(id).classList.add('hidden');

// === 4. 留言板逻辑 ===

// 切换聊天框折叠/展开
function toggleChat() {
    const box = document.getElementById('projectChatBox');
    const icon = document.getElementById('chatToggleIcon');
    
    // 检查当前是否是收起状态 (看 translateY 是否存在且不为0)
    // 注意：我们在 HTML 里默认写了 style="... transform: translateY(355px);"
    const isCollapsed = box.style.transform.includes('355px'); 
    
    if (isCollapsed) {
        // 展开
        box.style.transform = 'translateY(0)';
        icon.classList.remove('fa-chevron-up');
        icon.classList.add('fa-chevron-down');
    } else {
        // 收起 (露出头部45px左右)
        box.style.transform = 'translateY(355px)';
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-up');
    }
}

// 渲染留言
function renderChat() {
    const chatList = document.getElementById('chatMessages');
    chatList.innerHTML = '';
    
    if (!currentProject || !currentProject.messages) {
        chatList.innerHTML = '<div class="text-center text-xs text-gray-400 mt-4">请先选择一个项目</div>';
        return;
    }

    if (currentProject.messages.length === 0) {
        chatList.innerHTML = '<div class="text-center text-xs text-gray-400 mt-4">暂无留言</div>';
        return;
    }

    currentProject.messages.forEach(msg => {
        // 判断是"我"发的还是"别人"发的 (模拟：根据当前角色)
        const isMe = msg.role === currentUserRole; 
        const alignClass = isMe ? 'ml-auto bg-blue-100 text-blue-900' : 'mr-auto bg-white text-gray-800 border border-gray-200';
        
        const div = document.createElement('div');
        div.className = `max-w-[85%] p-2 rounded-lg text-xs ${alignClass}`;
        div.innerHTML = `
            <div class="font-bold mb-1 opacity-70 flex justify-between gap-2">
                <span>${msg.role === 'manager' ? '项目经理' : '译员'}</span>
                <span class="font-normal scale-90">${msg.time}</span>
            </div>
            <div>${msg.text}</div>
        `;
        chatList.appendChild(div);
    });
    chatList.scrollTop = chatList.scrollHeight;
}

// 发送留言
function sendProjectMessage() {
    if (!currentProject) return alert('请先选择项目');
    
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;

    // 确保 messages 数组存在
    if (!currentProject.messages) currentProject.messages = [];

    currentProject.messages.push({
        role: currentUserRole, // 'manager' 或 'translator'
        text: text,
        time: new Date().toLocaleTimeString('zh-CN', {hour:'2-digit', minute:'2-digit'})
    });

    saveData();
    renderChat();
    input.value = '';
}

// 监听回车发送
document.getElementById('chatInput').addEventListener('keydown', (e) => {
    if(e.key === 'Enter') sendProjectMessage();
});

// 【关键】修改 selectProject 函数，每次切换项目时刷新聊天
// 请找到原本的 selectProject 函数，在最后加一行：
// renderChat();