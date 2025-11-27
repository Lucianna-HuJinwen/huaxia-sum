# 术语管理系统 API 接口对接说明

## 概述

已成功对接前后端术语管理相关的API接口，包括术语库管理和术语条目管理功能。

## 已实现的功能

### 1. 术语库管理 (Glossary)

#### 创建术语库
- **接口**: `POST /glossary/create`
- **参数**: 
  ```json
  {
    "title": "术语库名称",
    "sourceLanguage": "源语言",
    "targetLanguage": "目标语言", 
    "description": "描述"
  }
  ```

#### 查询术语库列表
- **接口**: `GET /glossary/list`
- **参数**: `pageNum`, `pageSize`, `title`(可选)
- **功能**: 支持分页和按标题模糊搜索

#### 术语库详情
- **接口**: `GET /glossary/detail`
- **参数**: `glossaryId`

#### 编辑术语库
- **接口**: `PUT /glossary/edit`
- **参数**:
  ```json
  {
    "glossaryId": 123,
    "title": "新标题",
    "description": "新描述"
  }
  ```

#### 删除术语库
- **接口**: `DELETE /glossary/delete`
- **参数**: `glossaryId`

### 2. 术语条目管理 (Term)

#### 添加术语
- **接口**: `POST /term/add`
- **参数**:
  ```json
  {
    "glossaryId": 123,
    "sourceTerm": "源语言术语",
    "targetTerm": "目标语言术语"
  }
  ```

#### 查询术语列表
- **接口**: `GET /term/list`
- **参数**: `glossaryId`, `pageNum`, `pageSize`, `keyword`(可选)
- **功能**: 支持分页和关键词搜索

#### 编辑术语
- **接口**: `PUT /term/edit`
- **参数**:
  ```json
  {
    "termId": 456,
    "sourceTerm": "新的源语言术语",
    "targetTerm": "新的目标语言术语"
  }
  ```

#### 删除术语
- **接口**: `DELETE /term/delete`
- **参数**: `termId`

#### 批量导入术语
- **接口**: `POST /term/add/batch`
- **参数**: `glossaryId` + `file` (multipart/form-data)
- **支持格式**: CSV, Excel

#### 批量删除术语
- **接口**: `DELETE /term/delete/batch`
- **参数**:
  ```json
  {
    "glossaryId": 123,
    "termIds": ["456", "789"]
  }
  ```

## 前端页面功能

### 术语库管理页面 (`terminology_management.html`)
- ✅ 创建术语库
- ✅ 术语库列表展示（分页）
- ✅ 搜索术语库
- ✅ 删除术语库
- ✅ 跳转到术语详情页面

### 术语详情页面 (`terminology_detail.html`)
- ✅ 显示术语库信息
- ✅ 术语列表展示（分页）
- ✅ 添加术语
- ✅ 编辑术语
- ✅ 删除术语
- ✅ 搜索术语
- ✅ 批量导入术语（文件上传）
- ✅ 导出术语为CSV

## API 调用示例

### 使用 CommonApi 对象

```javascript
// 创建术语库
const response = await CommonApi.Glossary.create({
    title: "科技术语库",
    sourceLanguage: "中文",
    targetLanguage: "英文",
    description: "科技领域专业术语"
});

// 获取术语库列表
const glossaries = await CommonApi.Glossary.list({
    pageNum: 1,
    pageSize: 10,
    title: "科技" // 可选的搜索关键词
});

// 添加术语
await CommonApi.Term.add({
    glossaryId: 123,
    sourceTerm: "人工智能",
    targetTerm: "Artificial Intelligence"
});

// 批量导入术语
const fileInput = document.getElementById('fileInput');
const file = fileInput.files[0];
await CommonApi.Term.addBatch({
    glossaryId: 123,
    file: file
});
```

## 注意事项

1. **认证**: 所有API请求都需要在Header中携带 `Authorization` token
2. **错误处理**: 前端已实现统一的错误处理和提示
3. **分页**: 术语库和术语列表都支持分页功能
4. **文件上传**: 批量导入支持CSV和Excel格式
5. **数据验证**: 前端已添加必要的表单验证

## 测试建议

1. 先测试术语库的创建、查询、编辑、删除功能
2. 然后测试术语的增删改查功能
3. 最后测试批量导入导出功能
4. 验证分页和搜索功能是否正常工作

## 技术栈

- **前端**: 原生JavaScript + Tailwind CSS
- **后端**: Spring Boot + MyBatis-Plus
- **API规范**: RESTful API
- **数据格式**: JSON
