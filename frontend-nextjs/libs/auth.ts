import { decodeJWT } from '@/libs/decode';
import NextAuth, { CredentialsSignin, User } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import { JWT } from 'next-auth/jwt';
import { Session } from 'next-auth';
class InvalidLoginError extends CredentialsSignin {
  code = 'Invalid identifier or password';
}
export const {
  handlers: { GET, POST },
  auth,
} = NextAuth({
  providers: [
    CredentialsProvider({
      credentials: {
        username: { label: 'Username', type: 'text' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        try {
          const baseURL = process.env.NEXT_PUBLIC_BASE_URL || '';
          const apiUrl = `${baseURL}/auth/token`;
          console.log(apiUrl);
          const authResponse = await fetch(apiUrl, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
              username: credentials?.username as string,
              password: credentials?.password as string,
            }),
          });
          console.log(authResponse);

          if (!authResponse.ok) {
            throw new InvalidLoginError('Authentication request failed');
          }

          const jwtToken = await authResponse.json();
          const decodedToken = decodeJWT(jwtToken.access_token);
          return {
            accessToken: jwtToken.access_token,
            tokenType: jwtToken.token_type,
            id: decodedToken.sub,
            name: decodedToken.sub,
            email: decodedToken.sub,
            user: {
              accessToken: jwtToken.access_token,
              name: decodedToken.sub,
              email: decodedToken.sub,
            },
            token: {
              accessToken: jwtToken.access_token,
            },
          };
        } catch (error) {
          throw new InvalidLoginError('Failed to login');
        }
      },
    }),
  ],
  pages: {
    signIn: '/protected',
  },
  callbacks: {
    jwt({ token, user }: { token: JWT; user: User }) {
      if (user !== null && user !== undefined && 'accessToken' in user) {
        // eslint-disable-next-line no-param-reassign
        token.accessToken = user.accessToken as string;
      }

      return token;
    },
    async session({ session, token }: { session: Session; token: JWT }) {
      if (token?.accessToken) {
        session.accessToken = token.accessToken;
      }
      return session;
    },
  },
  session: {
    strategy: 'jwt',
  },
  trustHost: true,
  secret: process.env.NEXT_PUBLIC_AUTH_SECRET,
});

declare module 'next-auth' {
  interface Session {
    accessToken?: string;
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken?: string;
  }
}
