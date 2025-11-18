import React, { useMemo } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { BarChart3 } from 'lucide-react';

const EventsChart = ({ events, type = 'line' }) => {
  const chartData = useMemo(() => {
    if (!events || events.length === 0) return [];

    // Group events by hour
    const eventsByHour = events.reduce((acc, event) => {
      const date = new Date(event.createdAt);
      const hour = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours());
      const hourKey = hour.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit'
      });

      if (!acc[hourKey]) {
        acc[hourKey] = { time: hourKey, count: 0, timestamp: hour.getTime() };
      }
      acc[hourKey].count++;
      return acc;
    }, {});

    // Sort by timestamp and return array
    return Object.values(eventsByHour)
      .sort((a, b) => a.timestamp - b.timestamp)
      .slice(-24); // Last 24 hours
  }, [events]);

  if (!events || events.length === 0) {
    return (
      <div className="card">
        <div className="text-center text-gray-500 py-12">
          <BarChart3 className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>No data available for chart</p>
        </div>
      </div>
    );
  }

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white p-3 shadow-lg rounded-lg border border-gray-200">
          <p className="text-sm font-medium text-gray-900">{payload[0].payload.time}</p>
          <p className="text-sm text-primary-600">Events: {payload[0].value}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="card">
      <div className="flex items-center mb-4">
        <BarChart3 className="w-5 h-5 text-primary-600 mr-2" />
        <h3 className="text-lg font-semibold text-gray-900">Events Over Time</h3>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        {type === 'line' ? (
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="time"
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
            />
            <YAxis
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <Line
              type="monotone"
              dataKey="count"
              stroke="#3b82f6"
              strokeWidth={2}
              name="Events"
              dot={{ fill: '#3b82f6', r: 4 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        ) : (
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="time"
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
            />
            <YAxis
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <Bar
              dataKey="count"
              fill="#3b82f6"
              name="Events"
              radius={[4, 4, 0, 0]}
            />
          </BarChart>
        )}
      </ResponsiveContainer>
    </div>
  );
};

export default EventsChart;
