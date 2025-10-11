export function fetchWithAuth(url, options = {}, navigate) {
  return fetch(url, options).then(async res => {
    if (res.status === 403) {
      if (navigate) navigate('/login');
      // Optionally clear user session
      localStorage.clear();
      sessionStorage.clear();
      return Promise.reject(new Error('Forbidden'));
    }
    return res;
  });
}

