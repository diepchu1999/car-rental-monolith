import type {CustomerStatType} from "../types";

type CustomerStatIconsProps = {
    type: CustomerStatType;
};

export function CustomerStatIcons({
                                      type,
                                  }: CustomerStatIconsProps) {
    switch (type) {
        case "renter":
            return (
                <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                    <circle cx="10" cy="8" r="4" stroke="#3ECF8E" strokeWidth="1.2"/>
                    <path
                        d="M4 17c0-3.5 2.5-5 6-5s6 1.5 6 5"
                        stroke="#3ECF8E"
                        strokeWidth="1.2"
                        strokeLinecap="round"
                    />
                </svg>
            );

        case "host":
            return (
                <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                    <path
                        d="M4 16V8l6-4 6 4v8"
                        stroke="#C8A45C"
                        strokeWidth="1.2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                    <rect
                        x="7"
                        y="12"
                        width="6"
                        height="4"
                        rx=".5"
                        stroke="#C8A45C"
                        strokeWidth="1"
                    />
                </svg>
            );

        case "both":
            return (
                <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                    <circle cx="7" cy="8" r="3" stroke="#5BA4F5" strokeWidth="1.2"/>
                    <circle cx="14" cy="8" r="3" stroke="#5BA4F5" strokeWidth="1.2"/>
                    <path
                        d="M1 17c0-3 2-4.5 6-4.5M13 17c0-3-2-4.5-6-4.5"
                        stroke="#5BA4F5"
                        strokeWidth="1"
                        strokeLinecap="round"
                    />
                </svg>
            );
    }
}