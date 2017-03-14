import { Participant } from "./types";

export function sumTime(teamMembers: Array<Participant>) {
    return teamMembers.reduce((soFar, current) => (soFar + current.time), 0);
}

export function formatTime(numerical: number): string {
    const minutes = Math.floor(numerical / 60);
    const remaining = (numerical % 60).toFixed(2);

    return `${minutes}min ${remaining}sec`;
}