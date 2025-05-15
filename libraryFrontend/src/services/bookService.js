import apiClient from './api';

export default {
  getAllBooks(params) { // params 可以是 { title: '...', author: '...' }
    return apiClient.get('/books', { params });
  },
  getBookById(id) {
    return apiClient.get(`/books/${id}`);
  },
  createBook(bookData) {
    return apiClient.post('/books', bookData);
  },
  updateBook(id, bookData) {
    return apiClient.put(`/books/${id}`, bookData);
  },
  deleteBook(id) {
    return apiClient.delete(`/books/${id}`);
  },
  // 如果有图片上传，可能需要单独的函数或配置
  // uploadCoverImage(file) {
  //   const formData = new FormData();
  //   formData.append('image', file);
  //   return apiClient.post('/images/upload', formData, {
  //     headers: { 'Content-Type': 'multipart/form-data' }
  //   });
  // }
};