//import { MongoClient, ObjectId } from 'mongodb';
import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
//const hash = require("object-hash");
//const DEFAULT_TTL = 30;
//const DEFAULT_COLL = "cache";
//const uri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer";
//const client = new MongoClient(uri);


export function middleware(request: NextRequest) {
    // Getting cookies from the request using the `RequestCookies` API

    const response = NextResponse.next()
    //client.db().collection(DEFAULT_COLL).findOneAndUpdate({ _id: new ObjectId() }, { $set: { expires: new Date(new Date().getTime() + DEFAULT_TTL * 1000) } });
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