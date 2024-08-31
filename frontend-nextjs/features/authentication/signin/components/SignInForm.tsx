'use client';
import { Button, Box, Card, Flex, TextField, Heading, Text } from '@radix-ui/themes';
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
    <Flex minHeight="100%" align="center" justify="center">
      <Box width="100%" maxWidth="400px" p="4" mx="auto">
        <Card>
          <Heading as="h2" size="8" align="center" mb="4">
            Sign In to Your Account
          </Heading>
          <form onSubmit={handleSubmit}>
            <Flex direction="column" gap="4">
              <div>
                <label htmlFor="username">Username</label>
                <TextField.Root
                  id="username"
                  placeholder="usesrname"
                  value={username}
                  type="text"
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setUsername(e.target.value)
                  }
                  required
                />
              </div>
              <div>
                <label htmlFor="password">Password</label>
                <TextField.Root
                  id="password"
                  placeholder="Password"
                  value={password}
                  type="password"
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setPassword(e.target.value)
                  }
                  required
                />
              </div>
              <Flex justify="between" align="center">
                <Text size="4" color="ruby">
                  <input type="checkbox" id="remember-me" className="mr-2" />
                  <label htmlFor="remember-me">Remember me</label>
                </Text>
                <Text size="4">
                  <a href="#" className="text-primary hover:text-primary-foreground">
                    Forgot your password?
                  </a>
                </Text>
              </Flex>
              <Button type="submit" disabled={loading} color="ruby" size="4">
                {loading ? 'Signing in...' : 'Sign In'}
              </Button>
            </Flex>
          </form>
        </Card>
      </Box>
    </Flex>
  );
};

export default SignInForm;