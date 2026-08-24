import { IconButton, Tooltip } from '@mui/material';
import { useColorScheme } from '@mui/material/styles';
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined';
import LightModeOutlinedIcon from '@mui/icons-material/LightModeOutlined';

/**
 * Switches between the two colour schemes.
 *
 * The app starts on whichever one the operating system asks for, so this only
 * appears once the theme knows which that is. Rendering it earlier would show a
 * sun to someone whose machine is already dark.
 */
export function ColorSchemeToggle() {
  const { mode, colorScheme, setMode } = useColorScheme();

  if (!mode) return null;

  const goingDark = colorScheme !== 'dark';

  return (
    <Tooltip title={goingDark ? 'Switch to dark' : 'Switch to light'}>
      <IconButton
        color="inherit"
        aria-label={goingDark ? 'Switch to dark' : 'Switch to light'}
        onClick={() => setMode(goingDark ? 'dark' : 'light')}
      >
        {goingDark ? <DarkModeOutlinedIcon /> : <LightModeOutlinedIcon />}
      </IconButton>
    </Tooltip>
  );
}
