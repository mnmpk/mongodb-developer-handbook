import { destory, readSession } from "@/app/lib/session";
import { NextRequest, NextResponse } from "next/server";

export async function GET (
    req: NextRequest,
    res: NextResponse
) {
    await destory();
    return NextResponse.json(readSession());
}