// 页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('hxytt_token');
    if (!token) {
        alert('请先登录后再访问此页面');
        window.location.href = '../login.html';
        return;
    }
});

// 接入后端接口所需的页面级状态
let currentPage = 1;
let pageSize = 10;
let total = 0;
let totalPages = 1;
let searchKeyword = "";
let currentGlossaryId = null;
let currentGlossary = null;

// URL参数解析
function getUrlParams() {
    const params = {};
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    for (const [key, value] of urlParams) {
        params[key] = value;
    }
    return params;
}

// 加载并显示术语库信息
async function loadGlossaryInfo() {
    const params = getUrlParams();
    currentGlossaryId = params.glossaryId; // 保持为字符串，避免超大整数精度问题
    
    if (!currentGlossaryId) {
        showError('缺少术语库ID参数');
        return;
    }
    
    try {
        const response = await CommonApi.Glossary.detail(currentGlossaryId);
        if (response && response.data) {
            currentGlossary = response.data;
            displayGlossaryInfo(currentGlossary);
        } else {
            showError('获取术语库信息失败');
        }
    } catch (error) {
        console.error('加载术语库信息出错:', error);
        showError('加载术语库信息出错: ' + error.message);
    }
}

// 显示术语库信息
function displayGlossaryInfo(glossary) {
    if (glossary.title) {
        document.getElementById('currentTerminologyName').textContent = glossary.title;
        document.getElementById('terminologyTitle').textContent = glossary.title;
        document.title = `${glossary.title} - 华夏译典通`;
    }
}

// 术语数据
let termsData = [];

// 从后端加载术语数据
async function loadTermsData() {
    if (!currentGlossaryId) {
        console.error('缺少术语库ID');
        return;
    }
    
    try {
        const response = await CommonApi.Term.list({
            glossaryId: currentGlossaryId,
            pageNum: currentPage,
            pageSize: pageSize,
            keyword: searchKeyword || undefined
        });
        
        if (response && response.rows) {
            termsData = response.rows;
            total = response.total || 0;
            totalPages = Math.ceil(total / pageSize);
            renderTermsList();
            updatePaginationInfo();
        } else {
            console.error('加载术语列表失败:', response);
            showError('加载术语列表失败');
        }
    } catch (error) {
        console.error('加载术语列表出错:', error);
        showError('加载术语列表出错: ' + error.message);
    }
}

// 更新分页信息显示
function updatePaginationInfo() {
    const termCountElement = document.getElementById('termCount');
    const totalItemsCountElement = document.getElementById('totalItemsCount');
    
    if (termCountElement) {
        termCountElement.textContent = total;
    }
    if (totalItemsCountElement) {
        totalItemsCountElement.textContent = total;
    }
}

// 渲染术语列表
function renderTermsList() {
    const termsTableBody = document.getElementById('termsTableBody');
    const emptyState = document.getElementById('emptyState');
    const paginationControls = document.getElementById('paginationControls');
    
    // 清空表格内容
    termsTableBody.innerHTML = '';
    
    // 检查是否有术语
    if (termsData.length === 0) {
        emptyState.classList.remove('hidden');
        paginationControls.classList.add('hidden');
        return;
    }
    
    // 隐藏空状态，显示分页控件
    emptyState.classList.add('hidden');
    paginationControls.classList.remove('hidden');
    
    // 渲染术语行
    termsData.forEach((term, index) => {
        const row = document.createElement('tr');
        row.className = 'hover:bg-gray-50 transition-colors';
        
        // 状态样式映射
        const statusMap = {
            'normal': { text: '正常', bgClass: 'bg-green-100 text-green-800', icon: 'fa-check-circle' },
            'draft': { text: '草稿', bgClass: 'bg-blue-100 text-blue-800', icon: 'fa-pencil' },
            'review': { text: '待审核', bgClass: 'bg-yellow-100 text-yellow-800', icon: 'fa-clock-o' }
        };
        
        const statusInfo = statusMap[term.status] || statusMap['normal'];
        
        // 格式化创建时间
        const createTime = term.createTime ? new Date(term.createTime).toLocaleString('zh-CN') : '-';
        
        // 确保使用正确的ID字段
        const termId = term.termId || term.id;
        if (!termId) {
            console.error('术语缺少ID字段:', term);
            return;
        }
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <input type="checkbox" class="term-checkbox h-4 w-4 text-primary rounded border-gray-300 focus:ring-primary" data-id="${termId}">
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                ${(currentPage - 1) * pageSize + index + 1}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${term.sourceTerm || '-'}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${term.targetTerm || '-'}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusInfo.bgClass}">
                    <i class="fa ${statusInfo.icon} mr-1"></i>
                    ${statusInfo.text}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                ${createTime}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex space-x-2">
                    <button class="edit-term-btn text-primary hover:text-primary/80 transition-colors" data-id="${termId}">
                        <i class="fa fa-pencil"></i>
                    </button>
                    <button class="delete-term-btn text-gray-500 hover:text-red-600 transition-colors" data-id="${termId}">
                        <i class="fa fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        
        termsTableBody.appendChild(row);
    });
    
    // 添加编辑和删除按钮的事件监听
    addTermActionsListeners();
}

// 添加术语操作按钮的事件监听
function addTermActionsListeners() {
    // 编辑按钮
    document.querySelectorAll('.edit-term-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const termId = this.getAttribute('data-id');
            editTerm(termId);
        });
    });
    
    // 删除按钮
    document.querySelectorAll('.delete-term-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const termId = this.getAttribute('data-id');
            deleteTerm(termId);
        });
    });
    
    // 复选框
    document.querySelectorAll('.term-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectAllCheckbox();
        });
    });
}

// 更新全选复选框状态
function updateSelectAllCheckbox() {
    const allCheckboxes = document.querySelectorAll('.term-checkbox');
    const checkedCheckboxes = document.querySelectorAll('.term-checkbox:checked');
    const selectAllCheckbox = document.getElementById('selectAllTerms');
    
    selectAllCheckbox.checked = allCheckboxes.length > 0 && allCheckboxes.length === checkedCheckboxes.length;
    selectAllCheckbox.indeterminate = checkedCheckboxes.length > 0 && checkedCheckboxes.length < allCheckboxes.length;
}

// 打开添加术语弹窗
function openAddTermModal() {
    const termModal = document.getElementById('termModal');
    const modalTitle = document.getElementById('modalTitle');
    const termForm = document.getElementById('termForm');
    
    // 清空表单
    termForm.reset();
    
    // 设置标题
    modalTitle.textContent = '添加术语';
    
    // 移除可能存在的数据属性
    termForm.removeAttribute('data-id');
    
    // 显示弹窗
    termModal.classList.remove('hidden');
}

// 编辑术语
function editTerm(termId) {
    const termModal = document.getElementById('termModal');
    const modalTitle = document.getElementById('modalTitle');
    const termForm = document.getElementById('termForm');
    
    // 查找要编辑的术语
    const term = termsData.find(t => {
        const tId = t.termId || t.id;
        return tId && tId.toString() === termId.toString();
    });
    if (!term) {
        console.error('未找到要编辑的术语，termId:', termId, 'termsData:', termsData);
        showError('未找到要编辑的术语');
        return;
    }
    
    // 填充表单
    document.getElementById('sourceTerm').value = term.sourceTerm || '';
    document.getElementById('targetTerm').value = term.targetTerm || '';
    document.getElementById('termDefinition').value = term.definition || '';
    document.getElementById('termContext').value = term.context || '';
    document.getElementById('termStatus').value = term.status || 'normal';
    
    // 设置标题和数据属性
    modalTitle.textContent = '编辑术语';
    termForm.setAttribute('data-id', termId);
    
    // 显示弹窗
    termModal.classList.remove('hidden');
}

// 删除术语
async function deleteTerm(termId) {
    if (!confirm('确定要删除这个术语吗？')) {
        return;
    }
    
    // 验证termId
    if (!termId || termId === 'undefined' || termId === 'null') {
        console.error('无效的termId:', termId);
        showError('术语ID无效，无法删除');
        return;
    }
    
    console.log('准备删除术语，ID:', termId, '类型:', typeof termId);
    
    try {
        // 确保termId是数字类型
        console.log('发送删除请求，ID:', termId);
        await CommonApi.Term.delete(termId);
        showSuccess('术语删除成功！');
        // 重新加载术语列表
        await loadTermsData();
    } catch (error) {
        console.error('删除术语出错:', error);
        showError('删除术语失败: ' + error.message);
    }
}

// 保存术语
async function saveTerm(event) {
    event.preventDefault();
    
    const termForm = document.getElementById('termForm');
    const termModal = document.getElementById('termModal');
    const termId = termForm.getAttribute('data-id');
    
    // 获取表单数据
    const sourceTerm = document.getElementById('sourceTerm').value.trim();
    const targetTerm = document.getElementById('targetTerm').value.trim();
    const definition = document.getElementById('termDefinition').value.trim();
    const context = document.getElementById('termContext').value.trim();
    const status = document.getElementById('termStatus').value;
    
    // 验证表单
    if (!sourceTerm || !targetTerm) {
        showError('请填写源语言术语和目标语言术语');
        return;
    }
    
    if (!currentGlossaryId) {
        showError('缺少术语库ID');
        return;
    }
    
    try {
        if (termId) {
            // 编辑现有术语
            await CommonApi.Term.edit({
                termId: termId, // 保持字符串，后端可按Long解析
                sourceTerm,
                targetTerm
            });
            showSuccess('术语编辑成功！');
        } else {
            // 添加新术语
            await CommonApi.Term.add({
                glossaryId: currentGlossaryId, // 保持字符串
                sourceTerm,
                targetTerm
            });
            showSuccess('术语添加成功！');
        }
        
        // 关闭弹窗
        termModal.classList.add('hidden');
        
        // 重新加载术语列表
        await loadTermsData();
        
    } catch (error) {
        console.error('保存术语出错:', error);
        showError('保存术语失败: ' + error.message);
    }
}

// 搜索术语
async function searchTerms() {
    const searchInput = document.getElementById('termSearch');
    const filterSelect = document.getElementById('termFilter');
    
    if (searchInput) {
        searchKeyword = searchInput.value.trim();
    }
    
    // 重置到第一页
    currentPage = 1;
    
    // 重新加载数据
    await loadTermsData();
}

// 初始化页面
async function initPage() {
    // 加载术语库信息
    await loadGlossaryInfo();
    
    // 加载术语数据
    await loadTermsData();
    
    // 添加事件监听
    addEventListeners();
}

// 添加事件监听
function addEventListeners() {
    // 语言资产下拉菜单
    const languageAssetsBtn = document.getElementById('languageAssetsBtn');
    const languageAssetsDropdown = document.getElementById('languageAssetsDropdown');
    
    if (languageAssetsBtn && languageAssetsDropdown) {
        languageAssetsBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            languageAssetsDropdown.classList.toggle('hidden');
        });
    }
    
    // 用户下拉菜单
    const userBtn = document.getElementById('userBtn');
    const userDropdown = document.getElementById('userDropdown');
    
    if (userBtn && userDropdown) {
        userBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            userDropdown.classList.toggle('hidden');
        });
    }
    
    // 点击页面其他地方关闭下拉菜单
    document.addEventListener('click', function() {
        if (languageAssetsDropdown && !languageAssetsDropdown.classList.contains('hidden')) {
            languageAssetsDropdown.classList.add('hidden');
        }
        if (userDropdown && !userDropdown.classList.contains('hidden')) {
            userDropdown.classList.add('hidden');
        }
    });
    
    // 添加术语按钮
    const addTermBtn = document.getElementById('addTermBtn');
    const addFirstTermBtn = document.getElementById('addFirstTermBtn');
    
    if (addTermBtn) {
        addTermBtn.addEventListener('click', openAddTermModal);
    }
    
    if (addFirstTermBtn) {
        addFirstTermBtn.addEventListener('click', openAddTermModal);
    }
    
    // 关闭术语弹窗
    const closeTermModalBtn = document.getElementById('closeTermModalBtn');
    const cancelTermBtn = document.getElementById('cancelTermBtn');
    const termModal = document.getElementById('termModal');
    
    if (closeTermModalBtn) {
        closeTermModalBtn.addEventListener('click', function() {
            termModal.classList.add('hidden');
        });
    }
    
    if (cancelTermBtn) {
        cancelTermBtn.addEventListener('click', function() {
            termModal.classList.add('hidden');
        });
    }
    
    // 点击弹窗外部关闭弹窗
    if (termModal) {
        termModal.addEventListener('click', function(e) {
            if (e.target === termModal) {
                termModal.classList.add('hidden');
            }
        });
    }
    
    // 保存术语表单
    const termForm = document.getElementById('termForm');
    
    if (termForm) {
        termForm.addEventListener('submit', saveTerm);
    }
    
    // 搜索和筛选
    const termSearch = document.getElementById('termSearch');
    const termFilter = document.getElementById('termFilter');
    
    if (termSearch) {
        termSearch.addEventListener('input', searchTerms);
    }
    
    if (termFilter) {
        termFilter.addEventListener('change', searchTerms);
    }
    
    // 全选复选框
    const selectAllTerms = document.getElementById('selectAllTerms');
    
    if (selectAllTerms) {
        selectAllTerms.addEventListener('change', function() {
            const isChecked = this.checked;
            document.querySelectorAll('.term-checkbox').forEach(checkbox => {
                checkbox.checked = isChecked;
            });
        });
    }
    
    // 导入导出按钮
    const importTermsBtn = document.getElementById('importTermsBtn');
    const exportTermsBtn = document.getElementById('exportTermsBtn');
    
    if (importTermsBtn) {
        importTermsBtn.addEventListener('click', function() {
            openImportModal();
        });
    }
    
    if (exportTermsBtn) {
        exportTermsBtn.addEventListener('click', function() {
            exportTerms();
        });
    }
    
    // 高级搜索按钮
    const advancedSearchBtn = document.getElementById('advancedSearchBtn');
    
    if (advancedSearchBtn) {
        advancedSearchBtn.addEventListener('click', function() {
            alert('高级搜索功能正在开发中');
        });
    }
    
    // 分页按钮
    const prevPageBtn = document.getElementById('prevPageBtn');
    const nextPageBtn = document.getElementById('nextPageBtn');
    
    if (prevPageBtn) {
        prevPageBtn.addEventListener('click', async function() {
            if (currentPage > 1) {
                currentPage--;
                await loadTermsData();
            }
        });
    }
    
    if (nextPageBtn) {
        nextPageBtn.addEventListener('click', async function() {
            if (currentPage < totalPages) {
                currentPage++;
                await loadTermsData();
            }
        });
    }
}

// 显示错误信息
function showError(message) {
    alert('错误: ' + message);
}

// 显示成功信息
function showSuccess(message) {
    alert('成功: ' + message);
}

// 打开导入弹窗
function openImportModal() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls';
    input.onchange = handleFileImport;
    input.click();
}

// 处理文件导入
async function handleFileImport(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (!currentGlossaryId) {
        showError('缺少术语库ID');
        return;
    }
    
    try {
        await CommonApi.Term.addBatch({
            glossaryId: currentGlossaryId, // 保持字符串
            file: file
        });
        showSuccess('术语导入成功！');
        // 重新加载术语列表
        await loadTermsData();
    } catch (error) {
        console.error('导入术语出错:', error);
        showError('导入术语失败: ' + error.message);
    }
}

// 导出术语
async function exportTerms() {
    if (!currentGlossaryId) {
        showError('缺少术语库ID');
        return;
    }
    
    try {
        // 使用导出接口获取所有术语数据
        const response = await CommonApi.Term.export({
            glossaryId: currentGlossaryId,
            keyword: searchKeyword || undefined
        });
        
        if (!response || !response.rows || response.rows.length === 0) {
            showError('没有可导出的术语数据');
            return;
        }
        
        const allTerms = response.rows;
        
        // 创建CSV内容
        const headers = ['源语言术语', '目标语言术语', '定义', '上下文', '状态', '创建时间'];
        const csvContent = [
            headers.join(','),
            ...allTerms.map(term => [
                csvCell(term.sourceTerm),
                csvCell(term.targetTerm),
                csvCell(term.definition || ''),
                csvCell(term.context || ''),
                csvCell(term.status || 'normal'),
                csvCell(term.createTime ? new Date(term.createTime).toLocaleString('zh-CN') : '')
            ].join(','))
        ].join('\n');
        
        // 创建并下载文件
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
        const filename = `${currentGlossary?.title || '术语库'}_术语导出_${new Date().toISOString().slice(0, 10)}.csv`;
        downloadBlob(filename, blob);
        
        showSuccess(`成功导出 ${allTerms.length} 条术语数据`);
        
    } catch (error) {
        console.error('导出术语出错:', error);
        showError('导出术语失败: ' + error.message);
    }
}

// 下载 Blob 为文件
function downloadBlob(filename, blob) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

// CSV 单元格转义
function csvCell(v) {
    if (v == null) return "";
    const s = String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

// CSV 解析（导入用）
function parseCSV(text) {
    const lines = text.split(/\r?\n/).filter(l => l.trim() !== "");
    if (!lines.length) return [];

    let headers = lines[0].split(",").map(h => h.trim());
    let startIdx = 1;
    const known = ["sourceTerm","targetTerm","definition","context","status"];
    const hasHeader = headers.every(h => known.includes(h));
    if (!hasHeader) {
        headers = ["sourceTerm","targetTerm","definition","context","status"];
        startIdx = 0;
    }

    const rows = [];
    for (let i = startIdx; i < lines.length; i++) {
        const raw = lines[i];
        const cols = [];
        let cur = "", inQuote = false;
        for (let j = 0; j < raw.length; j++) {
            const ch = raw[j];
            if (ch === '"') {
                if (inQuote && raw[j+1] === '"') { cur += '"'; j++; } else { inQuote = !inQuote; }
            } else if (ch === ',' && !inQuote) {
                cols.push(cur); cur = "";
            } else {
                cur += ch;
            }
        }
        cols.push(cur);

        const obj = {};
        for (let k = 0; k < headers.length; k++) {
            obj[headers[k]] = (cols[k] ?? "").trim();
        }
        if (obj.sourceTerm || obj.targetTerm) rows.push(obj);
    }
    return rows;
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', initPage);
