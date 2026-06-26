import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});


function cleanJavaTypes(data) {
  if (Array.isArray(data)) {
    
    if (data.length === 2 && typeof data[0] === 'string' && (data[0].includes('java.') || data[0].includes('org.example.'))) {
      return cleanJavaTypes(data[1]); 
    }
    
    return data.map(item => cleanJavaTypes(item));
  } else if (data !== null && typeof data === 'object') {
    
    const cleanedObj = {};
    for (const key in data) {
      cleanedObj[key] = cleanJavaTypes(data[key]);
    }
    return cleanedObj;
  }
  
  return data;
}


api.interceptors.response.use(response => {
  response.data = cleanJavaTypes(response.data);
  return response;
});