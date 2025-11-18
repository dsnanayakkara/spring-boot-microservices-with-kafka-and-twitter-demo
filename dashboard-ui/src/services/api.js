import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8084';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const eventsApi = {
  // Get all events with pagination
  getAllEvents: async (page = 0, size = 20, sort = 'createdAt,desc') => {
    const response = await apiClient.get('/api/v1/events', {
      params: { page, size, sort }
    });
    return response.data;
  },

  // Search events by text
  searchEvents: async (text, page = 0, size = 20) => {
    const response = await apiClient.get('/api/v1/events/search', {
      params: { text, page, size }
    });
    return response.data;
  },

  // Get event by ID
  getEventById: async (id) => {
    const response = await apiClient.get(`/api/v1/events/${id}`);
    return response.data;
  },

  // Get events by user ID
  getEventsByUserId: async (userId) => {
    const response = await apiClient.get(`/api/v1/events/user/${userId}`);
    return response.data;
  },
};

export const healthApi = {
  // Check service health
  checkHealth: async (port) => {
    try {
      const response = await axios.get(`http://localhost:${port}/actuator/health`);
      return response.data;
    } catch (error) {
      return { status: 'DOWN', error: error.message };
    }
  },

  // Get all service health statuses
  checkAllServices: async () => {
    const services = [
      { name: 'Event Stream Service', port: 8080 },
      { name: 'Consumer Service', port: 8081 },
      { name: 'Streams Service', port: 8082 },
      { name: 'Elasticsearch Service', port: 8083 },
      { name: 'REST API Service', port: 8084 },
    ];

    const healthChecks = await Promise.all(
      services.map(async (service) => ({
        ...service,
        health: await healthApi.checkHealth(service.port),
      }))
    );

    return healthChecks;
  },
};

export default apiClient;
