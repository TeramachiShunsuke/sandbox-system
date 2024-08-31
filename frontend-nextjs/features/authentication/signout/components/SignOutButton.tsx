'use client';
import { Button } from '@radix-ui/themes';
import { useSignout } from '../hooks/useSignout';

export const SignOutButton = () => {
  const { loading, signout } = useSignout();
  const handleClick = async () => {
    await signout();
  };

  return (
    <Button radius="medium" loading={loading} onClick={handleClick}>
      Sign Out
    </Button>
  );
};
