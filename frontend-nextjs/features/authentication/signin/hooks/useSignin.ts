'use client';

import { signIn } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

export const useSignin = () => {
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    setLoading(() => true);
    e.preventDefault();
    const result = await signIn('credentials', {
      username,
      password,
      // callbackUrl: "/protected",
      redirect: false,
    });
    setLoading(() => false);
    if (result?.error) {
      console.error(result);
      console.error(result.error);
      console.error('Failed to sign in');
    } else {
      router.push('/protected');
    }
  };

  return {
    username,
    setUsername,
    password,
    setPassword,
    loading,
    setLoading,
    handleSubmit,
  };
};
