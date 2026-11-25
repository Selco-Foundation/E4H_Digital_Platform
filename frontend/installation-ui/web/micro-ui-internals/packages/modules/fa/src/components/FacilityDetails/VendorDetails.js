import React, { useMemo, useState } from "react";
import { Button, Loader, PopUp } from "@egovernments/digit-ui-react-components";
import Section from "./Section";
import ConfirmChangesModal from "../ConfirmChangesModal";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import useFacilityVendors from "../../hooks/useFacilityVendors";
import useVendorOrganisations from "../../hooks/useVendorOrganisations";
import { VendorOrganisationService } from "../../services/VendorOrganisation";
import { vendorChanges } from "../../utilities/VendorOrganisations";
import { apiErrorText } from "../../utilities/FacilityEdit";
import CommonUtils from "../../utilities/CommonUtils";

const linkButtonStyle = {
  background: "transparent",
  border: "none",
  color: "#C84C0E",
  cursor: "pointer",
  fontSize: "16px",
  fontWeight: 700,
  padding: 0,
};

/**
 * Picks the facility's vendor organisations: a search box over a checkbox list. Organisation
 * names are free text, so they are shown as they are and never passed through t(); DIGIT's
 * MultiSelectDropdown translates its labels and searches by translated code, so it isn't used.
 */
const VendorPicker = ({ t, initial, onCancel, onSave }) => {
  const { isLoading, data: vendors } = useVendorOrganisations();
  const [search, setSearch] = useState("");
  const [selectedCodes, setSelectedCodes] = useState(() => new Set(initial.map((organisation) => organisation.code)));

  // Mapped organisations stay listed even if no longer active, so they can still be removed.
  const options = useMemo(() => {
    const byCode = new Map(vendors.map((organisation) => [organisation.code, organisation]));
    initial.forEach((organisation) => byCode.has(organisation.code) || byCode.set(organisation.code, organisation));
    return [...byCode.values()].sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: "base" }));
  }, [vendors, initial]);

  const term = search.trim().toLowerCase();
  const visible = term
    ? options.filter((organisation) => organisation.name.toLowerCase().includes(term) || organisation.code.toLowerCase().includes(term))
    : options;

  const toggle = (code) => {
    const next = new Set(selectedCodes);
    next.has(code) ? next.delete(code) : next.add(code);
    setSelectedCodes(next);
  };

  return (
    <PopUp>
      <div
        role="dialog"
        aria-modal="true"
        aria-label={t("FA_EDIT_VENDOR_DETAILS")}
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "560px",
          maxWidth: "95%",
          padding: "24px",
          borderRadius: "5px",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "12px" }}>
          <h2 style={{ margin: 0, fontSize: "24px", fontWeight: 700, fontFamily: "Roboto", color: "#0B0C0C" }}>
            {t("FA_EDIT_VENDOR_DETAILS")}
          </h2>
          <button
            type="button"
            aria-label={t("CORE_COMMON_CLOSE")}
            onClick={onCancel}
            style={{ cursor: "pointer", backgroundColor: "#D6D5D4", display: "flex", alignItems: "center", padding: 0, borderRadius: "3px", border: "none" }}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>
        <p style={{ margin: "0 0 16px", fontSize: "16px", color: "#505A5F" }}>{t("FA_EDIT_VENDOR_DETAILS_DESC")}</p>
        <input
          type="text"
          className="employee-card-input"
          aria-label={t("FA_SEARCH_VENDOR_ORGANISATIONS")}
          placeholder={t("FA_SEARCH_VENDOR_ORGANISATIONS")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ width: "100%", margin: "0 0 8px" }}
        />
        <div style={{ fontSize: "14px", color: "#505A5F", marginBottom: "8px" }}>
          {t("FA_VENDOR_ORGANISATIONS_SELECTED", { count: selectedCodes.size })}
        </div>
        <div style={{ maxHeight: "320px", overflow: "auto", border: "1px solid #D6D5D4", borderRadius: "4px" }}>
          {isLoading ? (
            <Loader />
          ) : visible.length === 0 ? (
            <div style={{ padding: "12px", color: "#505A5F" }}>{t("FA_NO_VENDOR_ORGANISATIONS_FOUND")}</div>
          ) : (
            visible.map((organisation) => (
              <label
                key={organisation.code}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "12px",
                  padding: "10px 12px",
                  cursor: "pointer",
                  borderBottom: "1px solid #EEEEEE",
                  backgroundColor: selectedCodes.has(organisation.code) ? "#FBEEE8" : "white",
                }}
              >
                <input
                  type="checkbox"
                  checked={selectedCodes.has(organisation.code)}
                  onChange={() => toggle(organisation.code)}
                  style={{ width: "18px", height: "18px", accentColor: "#C84C0E" }}
                />
                <span>{organisation.name}</span>
              </label>
            ))
          )}
        </div>
        <div style={{ display: "flex", justifyContent: "flex-end", gap: "16px", marginTop: "24px" }}>
          <Button variation={"secondary"} label={t("CORE_COMMON_CANCEL")} onButtonClick={onCancel} />
          <Button
            variation={"primary"}
            label={t("CORE_COMMON_SAVE")}
            onButtonClick={() => onSave(options.filter((organisation) => selectedCodes.has(organisation.code)))}
          />
        </div>
      </div>
    </PopUp>
  );
};

const OrganisationList = ({ organisations }) => (
  <ul style={{ margin: "4px 0 0", paddingLeft: "20px" }}>
    {organisations.map((organisation) => (
      <li key={organisation.code}>{organisation.name}</li>
    ))}
  </ul>
);

/**
 * Facility details' Vendor Details: the vendor organisations mapped to the facility, with its
 * own Edit. A change here decides who can see the facility's tickets, so it is confirmed apart
 * from the facility Edit.
 */
const VendorDetails = ({ t, facilityId, setToast }) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { isLoading, isError, data: mapped, revalidate } = useFacilityVendors(facilityId);
  const [editing, setEditing] = useState(false);
  const [pending, setPending] = useState(null);
  const [saving, setSaving] = useState(false);

  const review = (selected) => {
    const changes = vendorChanges(mapped, selected);
    if (!changes.added.length && !changes.removed.length) {
      setEditing(false);
      return;
    }
    setPending({ selected, ...changes });
  };

  const save = async () => {
    const { selected } = pending;
    setPending(null);
    setSaving(true);
    try {
      await VendorOrganisationService.updateFacilityVendors(tenantId, facilityId, selected.map((organisation) => organisation.code));
      await revalidate();
      setEditing(false);
      setToast({ key: "success", label: t("FA_VENDOR_DETAILS_UPDATED") });
    } catch (error) {
      console.error("Error updating vendor organisations", error);
      // The backend names each organisation it refused; one request can carry several errors.
      setToast({ key: "error", label: apiErrorText(error) || CommonUtils.getApiErrorMessage(error) || t("FA_VENDOR_DETAILS_UPDATE_FAILED") });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Section
      title={
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span>{t("FA_VENDOR_DETAILS")}</span>
          <button type="button" style={linkButtonStyle} onClick={() => setEditing(true)} disabled={isLoading || isError}>
            {t("CORE_COMMON_EDIT")}
          </button>
        </div>
      }
    >
      {saving && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000020,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}
      <div style={{ display: "flex", marginBottom: "10px", gap: "15px" }}>
        <div style={{ fontWeight: "bold", width: "50%" }}>{t("FA_VENDOR_ORGANISATIONS")}</div>
        <div>
          {isLoading ? (
            <Loader />
          ) : isError ? (
            // Not "Not Mapped": the mapping is unknown, not empty.
            <span style={{ color: "#B91900" }}>{t("FA_VENDOR_DETAILS_LOAD_FAILED")}</span>
          ) : mapped.length ? (
            mapped.map((organisation) => <div key={organisation.code}>{organisation.name}</div>)
          ) : (
            t("FA_VENDOR_NOT_MAPPED")
          )}
        </div>
      </div>
      {editing && <VendorPicker t={t} initial={mapped} onCancel={() => setEditing(false)} onSave={review} />}
      {pending && (
        <ConfirmChangesModal
          t={t}
          title={"FA_CONFIRM_VENDOR_CHANGES"}
          confirmLabel={"FA_SAVE_CHANGES"}
          warnings={[
            ...(pending.removed.length ? [t("FA_WARN_VENDOR_REMOVED")] : []),
            ...(pending.selected.length ? [] : [t("FA_WARN_FACILITY_UNMAPPED")]),
          ]}
          onConfirm={save}
          onCancel={() => setPending(null)}
        >
          {pending.added.length > 0 && (
            <div style={{ marginBottom: "12px" }}>
              <strong>{t("FA_VENDORS_ADDED")}</strong>
              <OrganisationList organisations={pending.added} />
            </div>
          )}
          {pending.removed.length > 0 && (
            <div>
              <strong>{t("FA_VENDORS_REMOVED")}</strong>
              <OrganisationList organisations={pending.removed} />
            </div>
          )}
        </ConfirmChangesModal>
      )}
    </Section>
  );
};

export default VendorDetails;
