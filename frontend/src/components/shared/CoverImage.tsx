import { useState } from 'react';
import { Box } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';

interface CoverImageProps {
  src?: string | null;
  alt?: string;
  /** Width over height, so 16 / 9 is a wide banner and 1 is a square. */
  ratio?: number;
  /** Darkens the foot of the picture, for callers writing text over it. */
  scrim?: boolean;
  /** Dark when something light is drawn on top, which cream cannot carry. */
  placeholderTone?: 'light' | 'dark';
}

/**
 * A picture held to a fixed shape, whatever shape the picture itself is.
 */
export function CoverImage({
  src,
  alt = '',
  ratio = 4 / 3,
  scrim = false,
  placeholderTone = 'light',
}: CoverImageProps) {
  const [failed, setFailed] = useState(false);
  const showPlaceholder = !src || failed;
  const dark = placeholderTone === 'dark';

  return (
    <Box
      sx={{
        width: '100%',
        aspectRatio: String(ratio),
        bgcolor: dark ? 'primary.dark' : 'primary.light',
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}
    >
      {showPlaceholder ? (
        <RestaurantMenuIcon
          sx={{
            fontSize: 40,
            color: dark ? 'common.white' : 'primary.main',
            opacity: dark ? 0.28 : 0.35,
          }}
        />
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

      {scrim && (
        <Box
          aria-hidden
          sx={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(180deg, transparent 40%, rgba(28,25,23,.72) 100%)',
          }}
        />
      )}
    </Box>
  );
}