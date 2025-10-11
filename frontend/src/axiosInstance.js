import axios from 'axios';

let navigate = null;

export const setNavigate = nav => { navigate = nav; };

const axiosInstance = axios.create({
  // You can set baseURL here if needed
  // baseURL: '/api',
  withCredentials: true,
});

axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 403) {
      localStorage.clear();
      sessionStorage.clear();
      if (navigate) navigate('/login');
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;

