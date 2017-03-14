import * as fs from "fs";
import { Participant } from "./types";

export function parseParticipants(fileName: string): Array<Participant> {

    return fs.readFileSync(fileName, "utf8")
        .split("\r\n")
        .slice(1)
        .map((row: string) => row.split(";"))
        .map(createParticipant);
}

function createParticipant([
    place,
    _,
    firstName,
    lastName,
    team,
    gender,
    time
]): Participant {
    return {
        place: parseInt(place),
        name: `${firstName} ${lastName}`,
        team,
        gender: gender === "H" ? "male" : "female",
        time: stringToNumber(time)
    };
}

function stringToNumber(time: string): number {
    const [hours, minutes, seconds] = time.split(":");

    const numerical = (parseInt(hours) * 3600) + (parseInt(minutes) * 60) + parseFloat(seconds);
    return numerical;
}