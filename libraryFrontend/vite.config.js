import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 导入 Node.js 的 path 模块



// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // 将 @ 配置为指向 src 目录
    },
  },
  // 你可能还需要配置 server.port 或其他选项
  // server: {
  //   port: 5173, // Vite 默认可能会用这个端口，你可以自定义
  // }
})