import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
    // Getting cookies from the request using the `RequestCookies` API

    const response = NextResponse.next()
    if (request.cookies.has('session')) {
        let cookie = request.cookies.get('session');
        //console.log(cookie);
    } else {
        // Setting cookies on the response using the `ResponseCookies` API
        /*response.cookies.set({
            name: 'session',
            value: '',
            path: '/',
        });*/
    }
    return response
}

export const config = {
    matcher: [
        /*
         * Match all request paths except for the ones starting with:
         * - api (API routes)
         * - _next/static (static files)
         * - _next/image (image optimization files)
         * - favicon.ico, sitemap.xml, robots.txt (metadata files)
         */
        '/((?!api|_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)',
    ],
}