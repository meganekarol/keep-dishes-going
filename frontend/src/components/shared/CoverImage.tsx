import { useState } from 'react';
import { Box } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';

interface CoverImageProps {
  src?: string | null;
  alt?: string;
  /** Width over height, so 16 / 9 is a wide banner and 1 is a square. */
  ratio?: number;
}

/**
 * A picture held to a fixed shape, whatever shape the picture itself is.
 */
export function CoverImage({ src, alt = '', ratio = 4 / 3 }: CoverImageProps) {
  const [failed, setFailed] = useState(false);
  const showPlaceholder = !src || failed;

  return (
    <Box
      sx={{
        width: '100%',
        aspectRatio: String(ratio),
        bgcolor: 'primary.light',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}
    >
      {showPlaceholder ? (
        <RestaurantMenuIcon sx={{ fontSize: 40, color: 'primary.main', opacity: 0.35 }} />
      ) : (
        <Box
          component="img"
          src={src}
          alt={alt}
          loading="lazy"
          onError={() => setFailed(true)}
          sx={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
        />
      )}
    </Box>
  );
}