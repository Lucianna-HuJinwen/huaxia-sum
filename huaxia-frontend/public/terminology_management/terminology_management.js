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
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;

// 语言资产下拉菜单交互
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

// 创建术语库弹窗交互
const createTerminologyBtn = document.getElementById('createTerminologyBtn');
const createTerminologyModal = document.getElementById('createTerminologyModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const cancelCreateBtn = document.getElementById('cancelCreateBtn');
const createTerminologyForm = document.getElementById('createTerminologyForm');
const terminologyTable = document.querySelector('#terminologyTable tbody');


if (createTerminologyBtn && createTerminologyModal) {
    // 点击创建术语库按钮打开弹窗
    createTerminologyBtn.addEventListener('click', function() {
        createTerminologyModal.classList.remove('hidden');
    });

    // 点击关闭按钮关闭弹窗
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', function() {
            createTerminologyModal.classList.add('hidden');
            // 清理编辑状态
            createTerminologyForm.removeAttribute('data-edit-id');
            document.querySelector('#createTerminologyModal h2').textContent = '创建术语库';
        });
    }

    // 点击取消按钮关闭弹窗
    if (cancelCreateBtn) {
        cancelCreateBtn.addEventListener('click', function() {
            createTerminologyModal.classList.add('hidden');
            // 清理编辑状态
            createTerminologyForm.removeAttribute('data-edit-id');
            document.querySelector('#createTerminologyModal h2').textContent = '创建术语库';
        });
    }

    // 表单提交逻辑
    if (createTerminologyForm) {
        createTerminologyForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            // 获取表单数据
            const terminologyName = document.getElementById('terminologyName').value.trim();
            const sourceLang = document.getElementById('sourceLang').value;
            const targetLang = document.getElementById('targetLang').value;
            const description = document.querySelector('textarea').value.trim();
            const status = document.querySelector('select').value;
            
            if (!terminologyName) {
                showError('请输入术语库名称');
                return;
            }
            
            if (sourceLang === targetLang) {
                showError('源语言和目标语言不能相同');
                return;
            }
            
            try {
                const editId = createTerminologyForm.getAttribute('data-edit-id');
                
                if (editId) {
                    // 编辑模式
                    await CommonApi.Glossary.edit({
                        glossaryId: editId,
                        title: terminologyName,
                        sourceLanguage: sourceLang,
                        targetLanguage: targetLang,
                        description: description
                    });
                    showSuccess('术语库编辑成功！');
                } else {
                    // 创建模式
                    await CommonApi.Glossary.create({
                        title: terminologyName,
                        sourceLanguage: sourceLang,
                        targetLanguage: targetLang,
                        description: description
                    });
                    showSuccess('术语库创建成功！');
                }
                
                // 清空表单和编辑状态
                createTerminologyForm.reset();
                createTerminologyForm.removeAttribute('data-edit-id');
                
                // 恢复弹窗标题
                document.querySelector('#createTerminologyModal h2').textContent = '创建术语库';
                
                // 关闭弹窗
                createTerminologyModal.classList.add('hidden');
                
                // 重新加载术语库列表
                loadTerminologyList(currentPage, pageSize);
                
            } catch (error) {
                console.error('操作术语库出错:', error);
                showError('操作术语库失败: ' + error.message);
            }
        });
    }

    // 点击弹窗外部关闭弹窗
    createTerminologyModal.addEventListener('click', function(e) {
        if (e.target === createTerminologyModal) {
            createTerminologyModal.classList.add('hidden');
            // 清理编辑状态
            createTerminologyForm.removeAttribute('data-edit-id');
            document.querySelector('#createTerminologyModal h2').textContent = '创建术语库';
        }
    });
}

// —— 新增：下载 Blob 为文件（导出用）
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

// —— 新增：CSV 单元格转义（导出用）
function csvCell(v) {
    if (v == null) return "";
    const s = String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

// —— 新增：CSV 解析（导入用）
// 支持表头：sourceTerm,targetTerm,definition,context,status
// 没表头则按前两列分别当 sourceTerm/targetTerm
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

// 加载术语库列表
async function loadTerminologyList(pageNum = 1, pageSize = 10) {
    try {
        const response = await CommonApi.Glossary.list({
            pageNum: pageNum,
            pageSize: pageSize
        });
        
        if (response && response.rows) {
            renderTerminologyTable(response.rows);
            updatePagination(response.total, pageNum, pageSize);
        } else {
            console.error('加载术语库列表失败:', response);
            showError('加载术语库列表失败');
        }
    } catch (error) {
        console.error('加载术语库列表出错:', error);
        showError('加载术语库列表出错: ' + error.message);
    }
}

// 渲染术语库表格
function renderTerminologyTable(terminologyList) {
    if (!terminologyTable) return;
    
    // 清空现有内容
    terminologyTable.innerHTML = '';
    
    if (!terminologyList || terminologyList.length === 0) {
        terminologyTable.innerHTML = `
            <tr>
                <td colspan="7" class="py-8 text-center text-gray-500">
                    <i class="fa fa-inbox text-4xl mb-2 block"></i>
                    暂无术语库数据
                </td>
            </tr>
        `;
        return;
    }
    
    terminologyList.forEach((item, index) => {
        const row = document.createElement('tr');
        row.className = 'border-b border-gray-100 hover:bg-gray-50 transition-colors';
        
        // 格式化日期
        const createTime = item.createTime ? new Date(item.createTime).toLocaleString('zh-CN') : '-';
        
        // 状态显示
        const statusClass = item.status === 'normal' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800';
        const statusIcon = item.status === 'normal' ? 'fa-check-circle' : 'fa-exclamation-circle';
        const statusText = item.status === 'normal' ? '正常' : '待完善';
        
        row.innerHTML = `
            <td class="py-3 px-4 text-sm text-gray-600">${(currentPage - 1) * pageSize + index + 1}</td>
            <td class="py-3 px-4 text-sm text-gray-800 font-medium">${item.title || '-'}</td>
            <td class="py-3 px-4 text-sm">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusClass}">
                    <i class="fa ${statusIcon} mr-1"></i>
                    ${statusText}
                </span>
            </td>
            <td class="py-3 px-4 text-sm text-gray-600">${item.sourceLanguage || '-'}</td>
            <td class="py-3 px-4 text-sm text-gray-600">${item.targetLanguage || '-'}</td>
            <td class="py-3 px-4 text-sm text-gray-600">${createTime}</td>
            <td class="py-3 px-4 text-sm">
                <div class="flex space-x-2">
                    <button class="text-primary hover:text-red-600 transition-colors" onclick="editTerminology('${item.glossaryId}')">
                        <i class="fa fa-pencil"></i>
                    </button>
                    <button class="text-gray-500 hover:text-gray-700 transition-colors" onclick="deleteTerminology('${item.glossaryId}')">
                        <i class="fa fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        
        // 添加点击事件（除了按钮）
        row.addEventListener('click', function(e) {
            if (!e.target.closest('button')) {
                window.location.href = `./terminology_detail.html?glossaryId=${item.glossaryId}&name=${encodeURIComponent(item.title)}`;
            }
        });
        row.style.cursor = 'pointer';
        
        terminologyTable.appendChild(row);
    });
}

// 更新分页信息
function updatePagination(total, pageNum, pageSize) {
    totalPages = Math.ceil(total / pageSize);
    currentPage = pageNum;
    
    const paginationInfo = document.querySelector('.text-sm.text-gray-500');
    if (paginationInfo) {
        paginationInfo.textContent = `共${total}条记录，当前第${pageNum}/${totalPages}页`;
    }
    
    // 更新分页按钮状态
    const prevBtn = document.querySelector('.fa-chevron-left').parentElement;
    const nextBtn = document.querySelector('.fa-chevron-right').parentElement;
    
    if (prevBtn) {
        prevBtn.disabled = pageNum <= 1;
        prevBtn.className = pageNum <= 1 ? 
            'px-3 py-1 border border-gray-300 rounded-md text-gray-500 cursor-not-allowed' :
            'px-3 py-1 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 cursor-pointer';
    }
    
    if (nextBtn) {
        nextBtn.disabled = pageNum >= totalPages;
        nextBtn.className = pageNum >= totalPages ? 
            'px-3 py-1 border border-gray-300 rounded-md text-gray-500 cursor-not-allowed' :
            'px-3 py-1 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 cursor-pointer';
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

// 编辑术语库
async function editTerminology(glossaryId) {
    try {
        // 获取术语库详情
        const response = await CommonApi.Glossary.detail(glossaryId);
        if (!response || !response.data) {
            showError('获取术语库信息失败');
            return;
        }
        
        const glossary = response.data;
        
        // 填充表单数据
        document.getElementById('terminologyName').value = glossary.title || '';
        document.getElementById('sourceLang').value = glossary.sourceLanguage || '';
        document.getElementById('targetLang').value = glossary.targetLanguage || '';
        document.querySelector('textarea').value = glossary.description || '';
        
        // 设置表单为编辑模式
        const form = document.getElementById('createTerminologyForm');
        form.setAttribute('data-edit-id', glossaryId);
        
        // 修改弹窗标题
        document.querySelector('#createTerminologyModal h2').textContent = '编辑术语库';
        
        // 显示弹窗
        document.getElementById('createTerminologyModal').classList.remove('hidden');
        
    } catch (error) {
        console.error('获取术语库详情出错:', error);
        showError('获取术语库详情失败: ' + error.message);
    }
}

// 删除术语库
async function deleteTerminology(glossaryId) {
    if (!confirm('确定要删除这个术语库吗？此操作不可恢复。')) {
        return;
    }
    
    try {
        await CommonApi.Glossary.delete(glossaryId);
        showSuccess('术语库删除成功');
        loadTerminologyList(currentPage, pageSize);
    } catch (error) {
        console.error('删除术语库出错:', error);
        showError('删除术语库失败: ' + error.message);
    }
}

// 搜索功能
function setupSearch() {
    const searchInput = document.querySelector('input[placeholder*="搜索"]');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                const keyword = this.value.trim();
                if (keyword) {
                    searchTerminology(keyword);
                } else {
                    loadTerminologyList(1, pageSize);
                }
            }, 500); // 防抖，500ms后执行搜索
        });
    }
}

// 搜索术语库
async function searchTerminology(keyword) {
    try {
        const response = await CommonApi.Glossary.list({
            pageNum: 1,
            pageSize: pageSize,
            title: keyword
        });
        
        if (response && response.rows) {
            renderTerminologyTable(response.rows);
            updatePagination(response.total, 1, pageSize);
            currentPage = 1;
        } else {
            console.error('搜索术语库失败:', response);
            showError('搜索术语库失败');
        }
    } catch (error) {
        console.error('搜索术语库出错:', error);
        showError('搜索术语库出错: ' + error.message);
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 加载术语库列表
    loadTerminologyList();
    
    // 设置搜索功能
    setupSearch();
    
    // 为分页按钮添加事件
    const prevBtn = document.querySelector('.fa-chevron-left').parentElement;
    const nextBtn = document.querySelector('.fa-chevron-right').parentElement;
    
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            if (currentPage > 1) {
                loadTerminologyList(currentPage - 1, pageSize);
            }
        });
    }
    
    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            if (currentPage < totalPages) {
                loadTerminologyList(currentPage + 1, pageSize);
            }
        });
    }
});
