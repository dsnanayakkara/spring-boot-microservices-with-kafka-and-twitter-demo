# Social Events Dashboard

A modern, real-time dashboard for visualizing and monitoring social events from the Kafka microservices architecture.

## Features

- **Real-time Event Monitoring** - Auto-refresh every 5 seconds
- **Event Search** - Full-text search across all events
- **Interactive Charts** - Visualize events over time with Recharts
- **Service Health Monitoring** - Real-time status of all microservices
- **Statistics Dashboard** - Total events, unique users, events per minute
- **Responsive Design** - Works on desktop, tablet, and mobile devices
- **Modern UI** - Built with React 18, Vite, and Tailwind CSS

## Prerequisites

- Node.js 18+ and npm
- Running backend services (REST API on port 8084)

## Quick Start

### Development Mode

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser to `http://localhost:3000`

### Production Build

1. Build the application:
```bash
npm run build
```

2. Preview the production build:
```bash
npm run preview
```

### Docker Deployment

1. Build the Docker image:
```bash
docker build -t social-events-dashboard .
```

2. Run the container:
```bash
docker run -p 80:80 social-events-dashboard
```

## Environment Variables

Create a `.env` file based on `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8084
```

## Project Structure

```
dashboard-ui/
├── src/
│   ├── components/          # React components
│   │   ├── EventsList.jsx   # Event list display
│   │   ├── SearchBar.jsx    # Search functionality
│   │   ├── StatsCard.jsx    # Statistics cards
│   │   ├── ServiceStatus.jsx # Service health monitor
│   │   └── EventsChart.jsx  # Recharts visualization
│   ├── services/            # API services
│   │   └── api.js          # REST API client
│   ├── App.jsx             # Main application
│   ├── main.jsx            # Entry point
│   └── index.css           # Global styles
├── public/                 # Static assets
├── index.html             # HTML template
├── vite.config.js         # Vite configuration
├── tailwind.config.js     # Tailwind CSS configuration
├── Dockerfile             # Docker configuration
└── nginx.conf             # Nginx configuration
```

## Features in Detail

### Real-time Updates
- Auto-refresh toggleable on/off
- Updates every 5 seconds when enabled
- Manual refresh button available

### Search & Filter
- Full-text search across event content
- Clear search to return to all events
- Paginated results

### Charts & Analytics
- Line chart showing events over time
- Last 24 hours of data
- Hourly aggregation
- Interactive tooltips

### Service Monitoring
- Health checks for all 5 microservices
- Status indicators (UP/DOWN)
- Auto-refresh every 10 seconds
- Port and service name display

### Statistics
- **Total Events** - All indexed events
- **Unique Users** - Count of distinct users
- **Events/Minute** - Recent activity rate
- **Avg Events/Hour** - Historical average

## API Integration

The dashboard connects to the REST API service on port 8084:

- `GET /api/v1/events` - Fetch all events (paginated)
- `GET /api/v1/events/search?text={text}` - Search events
- `GET /actuator/health` - Health checks for each service

## Technologies Used

- **React 18** - UI framework
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Recharts** - Charting library
- **Axios** - HTTP client
- **Lucide React** - Icon library

## Development

### Install Dependencies
```bash
npm install
```

### Run Dev Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
```

### Lint Code
```bash
npm run lint
```

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

This project is for educational purposes.
