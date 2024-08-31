import { auth } from '@/libs/auth';
import { decodeJWT } from '@/libs/decode';
import type { NextRequest } from 'next/server';
import { NextResponse } from 'next/server';

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - signin (signin page)
     */
    '/((?!api|_next/static|_next/image|favicon.ico|signin).*)',
  ],
};

export const middleware = async (request: NextRequest) => {
  const session = await auth();
  const url = new URL('/signin', request.nextUrl.origin);
  if (
    session === undefined ||
    session === null ||
    session.accessToken === undefined
  ) {
    return NextResponse.redirect(url);
  }
  if (decodeJWT(session.accessToken).exp * 1000 < Date.now()) {
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
};
