import { Button, Stack, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
      <Typography variant="h1">Nothing here</Typography>
      <Typography color="text.secondary">That page does not exist.</Typography>
      <Button variant="contained" component={Link} to="/restaurants">
        Back to the restaurants
      </Button>
    </Stack>
  );
}
