import { useState } from "react";
import { ImageOff } from "lucide-react";
import type { DocumentSide } from "../types";
import { documentSideLabel } from "../constants";

type Props = {
  side: DocumentSide;
  fileUrl: string;
  selected: boolean;
  onSelect: () => void;
};

export function KycDocumentThumbnail({ side, fileUrl, selected, onSelect }: Props) {
  const [broken, setBroken] = useState(false);
  return (
    <button
      type="button"
      className={`kyc-thumb${selected ? " is-selected" : ""}`}
      onClick={onSelect}
      title={documentSideLabel[side]}
    >
      <div className="kyc-thumb-img">
        {broken ? (
          <div className="kyc-thumb-fallback">
            <ImageOff size={18} />
          </div>
        ) : (
          <img src={fileUrl} alt={documentSideLabel[side]} onError={() => setBroken(true)} />
        )}
      </div>
      <span className="kyc-thumb-label">{documentSideLabel[side]}</span>
    </button>
  );
}
