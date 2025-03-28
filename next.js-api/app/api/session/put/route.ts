import { putSession, readSession } from "@/app/lib/session";
import { NextRequest, NextResponse } from "next/server";

export function GET (
    req: NextRequest,
    res: NextResponse
) {
    let session: any = readSession();
    if(session.shareData){
        session.shareData.search.push(req?.nextUrl?.searchParams.get('s'));
        session.shareData.views++;
        putSession(session);
    }
    return NextResponse.json(session);
}