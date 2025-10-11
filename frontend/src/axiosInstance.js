import axios from 'axios';

let navigate = null;

export const setNavigate = nav => { navigate = nav; };

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // Fixed: removed trailing slash and /api
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
