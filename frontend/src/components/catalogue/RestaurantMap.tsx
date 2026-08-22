import { useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Alert, Box, Button, Chip, Stack, Typography, useTheme } from '@mui/material';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import { divIcon } from 'leaflet';
import 'leaflet/dist/leaflet.css';

import { useCustomerLocation } from '../../hooks/useCustomerLocation';
import type { RestaurantSummary } from '../../types/api';
import { humanise } from '../../utils/format';

type Point = [number, number];

interface PlacedRestaurant {
  restaurant: RestaurantSummary;
  position: Point;
}

function pin(color: string) {
  return divIcon({
    // Blank, or Leaflet wraps the pin in its default white box.
    className: '',
    html: `<svg width="26" height="34" viewBox="0 0 26 34" xmlns="http://www.w3.org/2000/svg">
             <path d="M13 0C5.8 0 0 5.8 0 13c0 9.8 13 21 13 21s13-11.2 13-21c0-7.2-5.8-13-13-13z"
                   fill="${color}"/>
             <circle cx="13" cy="13" r="5" fill="#fff"/>
           </svg>`,
    iconSize: [26, 34],
    iconAnchor: [13, 34],
    popupAnchor: [0, -32],
  });
}

function dot(color: string) {
  return divIcon({
    className: '',
    html: `<span style="display:block;width:16px;height:16px;border-radius:50%;
                        background:${color};border:3px solid #fff;
                        box-shadow:0 0 0 1px rgba(0,0,0,.25)"></span>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
    popupAnchor: [0, -10],
  });
}

/**
 * Frames the map around everything on it.
 */
function FitToMarkers({ points }: { points: Point[] }) {
  const map = useMap();
  // Keyed on the positions rather than the array itself, which is new on every
  // render and would snap the map back while the customer is panning.
  const positions = points.map((point) => point.join(',')).join('|');

  useEffect(() => {
    if (!positions) return;

    const bounds = positions
      .split('|')
      .map((point) => point.split(',').map(Number) as Point);

    // maxZoom, or a single restaurant fills the screen with its own street.
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 });
  }, [map, positions]);

  return null;
}

/**
 * The catalogue as a map (US-14).
 */
export function RestaurantMap({ restaurants }: { restaurants: RestaurantSummary[] }) {
  const theme = useTheme();
  const { location } = useCustomerLocation();

  const placed: PlacedRestaurant[] = restaurants.flatMap((restaurant) => {
    const point = restaurant.address.coordinates;
    if (!point) return [];
    return [{ restaurant, position: [point.latitude, point.longitude] }];
  });

  const unplaced = restaurants.length - placed.length;

  if (placed.length === 0) {
    return (
      <Alert severity="info">
        None of these restaurants could be placed on the map. Try the list instead.
      </Alert>
    );
  }

  const here: Point | null = location ? [location.lat, location.lon] : null;
  const framed = here ? [...placed.map((entry) => entry.position), here] : placed.map((entry) => entry.position);

  return (
    <>
      <Box
        sx={{
          height: { xs: 420, md: 560 },
          borderRadius: 4,
          overflow: 'hidden',
          border: 1,
          borderColor: 'divider',
          // Leaflet ships its own font stack and link colour.
          '& .leaflet-container': { fontFamily: 'inherit' },
          '& .leaflet-popup-content': { m: 1.5, minWidth: 180 },
        }}
      >
        <MapContainer
          style={{ height: '100%', width: '100%' }}
          center={placed[0].position}
          zoom={13}
          scrollWheelZoom
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <FitToMarkers points={framed} />

          {here && (
            <Marker position={here} icon={dot(theme.palette.secondary.main)}>
              <Popup>You are here</Popup>
            </Marker>
          )}

          {placed.map(({ restaurant, position }) => (
            <Marker
              key={restaurant.restaurantId}
              position={position}
              icon={pin(restaurant.open ? theme.palette.primary.main : theme.palette.text.disabled)}
            >
              <Popup>
                <Stack spacing={0.5}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                      {restaurant.name}
                    </Typography>
                    <Chip
                      size="small"
                      label={restaurant.open ? 'Open' : 'Closed'}
                      color={restaurant.open ? 'success' : 'default'}
                    />
                  </Stack>

                  <Typography variant="body2" color="text.secondary">
                    {humanise(restaurant.cuisineType)} · {restaurant.address.street}{' '}
                    {restaurant.address.number}, {restaurant.address.city}
                  </Typography>

                  {restaurant.open && (
                    <Typography variant="body2" color="text.secondary">
                      Roughly {restaurant.guesstimatedDeliveryTime} minutes to your door
                    </Typography>
                  )}

                  <Button
                    size="small"
                    variant="contained"
                    component={RouterLink}
                    to={`/restaurants/${restaurant.restaurantId}`}
                    sx={{ mt: 1, alignSelf: 'flex-start' }}
                  >
                    View the menu
                  </Button>
                </Stack>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </Box>

      {unplaced > 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
          {unplaced} {unplaced === 1 ? 'restaurant is' : 'restaurants are'} missing from the map
          because their address could not be located. {unplaced === 1 ? 'It is' : 'They are'} in
          the list.
        </Typography>
      )}
    </>
  );
}