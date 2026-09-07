import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'
import {svelteTesting} from '@testing-library/svelte/vite'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [tailwindcss(), svelte(), svelteTesting()],
  resolve: {
    alias: {
      src: new URL('src', import.meta.url).pathname,
      i18n: new URL('i18n', import.meta.url).pathname
    },
  },
  server: {
    port: 8000,
    proxy: {
      '^/(api|oauth)': {
        target: 'http://localhost:8080',
        changeOrigin: false
      },
    }
  },
  build: {
    outDir: 'build',
    target: 'es2023',
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: 'src/setup-tests.ts'
  }
})
