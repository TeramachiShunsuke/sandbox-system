'use client';
import { Button, Box, Card, Flex, TextField } from '@radix-ui/themes';
import { ChangeEvent } from 'react';
import { useSignin } from '../hooks/useSignin';

const SignInForm = () => {
  const {
    username,
    setUsername,
    password,
    setPassword,
    handleSubmit,
    loading,
  } = useSignin();
  return (
    <Box maxWidth="400px">
      <Card>
        <h2>SignIn</h2>
        <Flex gap="3" align="center">
          <form onSubmit={handleSubmit} className="p-2 m-4">
            <div>
              <TextField.Root
                placeholder="username"
                value={username}
                type="text"
                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                  setUsername(e.target.value)
                }
              />
            </div>
            <div className='py-2'>
              <TextField.Root
                placeholder="password"
                value={password}
                type="password"
                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                  setPassword(e.target.value)
                }
              />
            </div>
            <div>
              {loading && <p>Loading...</p>}
              <Button type="submit">Sign In</Button>
            </div>
          </form>
        </Flex>
      </Card>
    </Box>
  );
};

export default SignInForm;
