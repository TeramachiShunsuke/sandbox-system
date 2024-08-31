'use client';

import { signOut } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

export const useSignout = () => {
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const signout = async () => {
    setLoading(() => true);
    await signOut({ redirect: false, callbackUrl: '/signin' }).finally(() => {
      setLoading(() => false);
      router.push('/signin');
    });
  };

  return { loading, signout };
};
