import React, { useState, useEffect, useCallback } from 'react';
import { Activity, MessageSquare, Users, TrendingUp, RefreshCw } from 'lucide-react';
import StatsCard from './components/StatsCard';
import EventsList from './components/EventsList';
import SearchBar from './components/SearchBar';
import ServiceStatus from './components/ServiceStatus';
import EventsChart from './components/EventsChart';
import { eventsApi } from './services/api';

function App() {
  const [events, setEvents] = useState([]);
  const [allEvents, setAllEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchMode, setSearchMode] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      const data = await eventsApi.getAllEvents(page, 50);

      if (data.content) {
        setEvents(data.content);
        setAllEvents(prev => {
          const newEvents = data.content.filter(
            e => !prev.some(pe => pe.id === e.id)
          );
          return [...newEvents, ...prev].slice(0, 100); // Keep last 100 events
        });
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      }
    } catch (error) {
      console.error('Failed to fetch events:', error);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  useEffect(() => {
    if (autoRefresh && !searchMode) {
      const interval = setInterval(fetchEvents, 5000); // Refresh every 5 seconds
      return () => clearInterval(interval);
    }
  }, [autoRefresh, searchMode, fetchEvents]);

  const handleSearch = async (text) => {
    try {
      setLoading(true);
      setSearchMode(true);
      const data = await eventsApi.searchEvents(text, 0, 50);
      if (data.content) {
        setEvents(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      }
    } catch (error) {
      console.error('Failed to search events:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchMode(false);
    setPage(0);
    fetchEvents();
  };

  const handleRefresh = () => {
    fetchEvents();
  };

  const toggleAutoRefresh = () => {
    setAutoRefresh(!autoRefresh);
  };

  // Calculate statistics
  const stats = {
    totalEvents: totalElements,
    uniqueUsers: new Set(allEvents.map(e => e.userId)).size,
    eventsPerMinute: allEvents.filter(e => {
      const eventTime = new Date(e.createdAt);
      const now = new Date();
      return (now - eventTime) < 60000; // Last minute
    }).length,
    avgEventsPerHour: Math.round(allEvents.length / 24),
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Activity className="w-8 h-8 text-primary-600" />
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Social Events Dashboard</h1>
                <p className="text-sm text-gray-500">Real-time event stream monitoring</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={toggleAutoRefresh}
                className={`flex items-center space-x-2 px-4 py-2 rounded-md transition-colors ${
                  autoRefresh
                    ? 'bg-green-50 text-green-700 border border-green-200'
                    : 'bg-gray-100 text-gray-600 border border-gray-200'
                }`}
              >
                <RefreshCw className={`w-4 h-4 ${autoRefresh ? 'animate-spin' : ''}`} />
                <span className="text-sm font-medium">
                  {autoRefresh ? 'Auto-refresh ON' : 'Auto-refresh OFF'}
                </span>
              </button>
              <button
                onClick={handleRefresh}
                className="btn-primary flex items-center space-x-2"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Refresh</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatsCard
            title="Total Events"
            value={stats.totalEvents.toLocaleString()}
            icon={MessageSquare}
            color="primary"
          />
          <StatsCard
            title="Unique Users"
            value={stats.uniqueUsers.toLocaleString()}
            icon={Users}
            color="green"
          />
          <StatsCard
            title="Events/Minute"
            value={stats.eventsPerMinute}
            icon={TrendingUp}
            color="blue"
          />
          <StatsCard
            title="Avg Events/Hour"
            value={stats.avgEventsPerHour}
            icon={Activity}
            color="purple"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Search Bar */}
            <SearchBar onSearch={handleSearch} onClear={handleClearSearch} />

            {/* Chart */}
            <EventsChart events={allEvents} type="line" />

            {/* Events List */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900">
                  {searchMode ? 'Search Results' : 'Recent Events'}
                </h2>
                <span className="text-sm text-gray-500">
                  Showing {events.length} of {totalElements} events
                </span>
              </div>
              <EventsList events={events} loading={loading} />

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="mt-6 flex justify-center">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => setPage(p => Math.max(0, p - 1))}
                      disabled={page === 0}
                      className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Previous
                    </button>
                    <span className="flex items-center px-4 text-sm text-gray-600">
                      Page {page + 1} of {totalPages}
                    </span>
                    <button
                      onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                      disabled={page >= totalPages - 1}
                      className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1">
            <ServiceStatus />
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-sm text-gray-500">
            Social Events Dashboard - Powered by Spring Boot, Kafka, and Elasticsearch
          </p>
        </div>
      </footer>
    </div>
  );
}

export default App;
