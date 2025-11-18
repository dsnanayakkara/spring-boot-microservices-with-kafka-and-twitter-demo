import React from 'react';
import { Calendar, User, MessageSquare } from 'lucide-react';

const EventsList = ({ events, loading }) => {
  if (loading) {
    return (
      <div className="card">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      </div>
    );
  }

  if (!events || events.length === 0) {
    return (
      <div className="card">
        <div className="text-center text-gray-500 py-12">
          <MessageSquare className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>No events found</p>
        </div>
      </div>
    );
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-4">
      {events.map((event) => (
        <div key={event.id} className="card hover:shadow-lg transition-shadow">
          <div className="flex items-start space-x-4">
            <div className="flex-shrink-0">
              <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
                <User className="w-5 h-5 text-primary-600" />
              </div>
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm font-medium text-gray-900">User {event.userId}</p>
                <div className="flex items-center text-sm text-gray-500">
                  <Calendar className="w-4 h-4 mr-1" />
                  {formatDate(event.createdAt)}
                </div>
              </div>
              <p className="text-gray-700 leading-relaxed">{event.text}</p>
              <div className="mt-3 flex items-center space-x-4 text-xs text-gray-500">
                <span className="bg-gray-100 px-2 py-1 rounded">ID: {event.id}</span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default EventsList;
