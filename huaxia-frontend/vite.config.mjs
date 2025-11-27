// // vite.config.mjs
// import { defineConfig } from "vite";
//
// const TARGET = "http://127.0.0.1:19200";  // ✅ 指向网关
//
// export default defineConfig({
//   server: {
//     port: 5173,
//     proxy: {
//       "/api": {
//         target: TARGET,
//         changeOrigin: true,
//         // 把 /api 去掉，直接转发给网关
//         rewrite: (p) => p.replace(/^\/api/, ""),
//       },
//     },
//   },
// });


import { defineConfig } from "vite";
import { resolve } from 'path';

const TARGET = "http://127.0.0.1:19200";
// const TARGET = "http://106.53.26.235:19200";

export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: TARGET,
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, ""),
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    rollupOptions: {
      input: {
        main: 'index.html',
        login: 'login.html',
        // 各功能模块
        sixiang: '思想流派/index.html',
        shici: '诗词歌赋/index.html',
        yinyue: '民俗音乐/index.html',
        feiyihtml: '非遗传承/index.html',
        yinshi: '饮食文化/index.html',
        jieri: '传统节日/index.html',
        // 术语管理
        terminology: 'terminology_management/terminology_management.html',
        terminologyDetail: 'terminology_management/terminology_detail.html',
        // 翻译功能
        translate: '翻译页面/translation_page.html',
        translateStats: '翻译统计管理/translation_statistics.html'
      },
      output: {
        manualChunks: undefined
      }
    }
  },
  // 指定public目录，Vite会自动将这些文件复制到dist目录
  publicDir: 'public'
});