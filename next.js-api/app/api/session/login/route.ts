import { connect } from "@/app/lib/database";
import { putSession, readSession } from "@/app/lib/session";
import { NextRequest, NextResponse } from "next/server";

export async function GET(
    req: NextRequest,
    res: NextResponse
) {
    let session: any = (await readSession()) || {};

    session.principal = req?.nextUrl?.searchParams.get('uId');

    //Populate other channel's data
    let temp = { channel: "web", views: 1, search: [] };
    const shareData = await (await connect()).collection('sessions').findOne({ "principal": session.principal, "attrs.shareData.channel": "mob" });
    if (shareData && shareData.attrs && shareData.attrs.shareData) {
        temp.search = temp.search.concat(shareData.attrs.shareData.search[1]);
        temp.views += shareData.attrs.shareData.views;
    }
    session.shareData = temp;
    return NextResponse.json(putSession(session));
}