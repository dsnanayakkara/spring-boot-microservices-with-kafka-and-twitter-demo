import React, { useState, useEffect } from 'react';
import { Activity, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { healthApi } from '../services/api';

const ServiceStatus = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkServices();
    const interval = setInterval(checkServices, 10000); // Check every 10 seconds
    return () => clearInterval(interval);
  }, []);

  const checkServices = async () => {
    try {
      const healthChecks = await healthApi.checkAllServices();
      setServices(healthChecks);
    } catch (error) {
      console.error('Failed to check services:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (health) => {
    if (!health || health.status === 'DOWN') {
      return <XCircle className="w-5 h-5 text-red-500" />;
    }
    if (health.status === 'UP') {
      return <CheckCircle className="w-5 h-5 text-green-500" />;
    }
    return <AlertCircle className="w-5 h-5 text-yellow-500" />;
  };

  const getStatusColor = (health) => {
    if (!health || health.status === 'DOWN') return 'bg-red-50 border-red-200';
    if (health.status === 'UP') return 'bg-green-50 border-green-200';
    return 'bg-yellow-50 border-yellow-200';
  };

  if (loading) {
    return (
      <div className="card">
        <div className="flex items-center justify-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="flex items-center mb-4">
        <Activity className="w-5 h-5 text-primary-600 mr-2" />
        <h3 className="text-lg font-semibold text-gray-900">Service Health</h3>
      </div>
      <div className="space-y-3">
        {services.map((service) => (
          <div
            key={service.port}
            className={`p-3 rounded-lg border ${getStatusColor(service.health)}`}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                {getStatusIcon(service.health)}
                <div>
                  <p className="text-sm font-medium text-gray-900">{service.name}</p>
                  <p className="text-xs text-gray-500">Port {service.port}</p>
                </div>
              </div>
              <span className={`text-xs font-medium px-2 py-1 rounded ${
                service.health?.status === 'UP'
                  ? 'bg-green-100 text-green-800'
                  : 'bg-red-100 text-red-800'
              }`}>
                {service.health?.status || 'DOWN'}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ServiceStatus;
