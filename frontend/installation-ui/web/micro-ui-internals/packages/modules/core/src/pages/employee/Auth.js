import React, {useEffect, useState} from "react";
import {Button, TextInput, CardText, PopUp} from "@egovernments/digit-ui-components";
import { OTPInput } from "@egovernments/digit-ui-react-components";

const Auth = () => {

    const [role, setRole] = useState("ADV"); // "ADV" | "JUDGE"
    const [phone, setPhone] = useState("");
    const isValidPhone = /^\d{10}$/.test(phone);

    // OTP popup state
    const [showOtp, setShowOtp] = useState(false);
    const [otp, setOtp] = useState("");
    const [timeLeft, setTimeLeft] = useState(30);
    const canResend = timeLeft === 0;

    useEffect(() => {
        if (!showOtp || timeLeft === 0) return;
        const id = setInterval(() => setTimeLeft((t) => (t > 0 ? t - 1 : 0)), 1000);
        return () => clearInterval(id);
    }, [showOtp, timeLeft]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!isValidPhone) return;
        // open OTP verification popup
        setShowOtp(true);
        setTimeLeft(30);
        setOtp("");
    };

    const handleVerifyOtp = () => {
        if (otp?.length !== 6) return;
        // demo success path
        setShowOtp(false);
        alert(`(demo) Phone +91 ${phone} verified`);
    };

    const handleResend = () => {
        if (!canResend) return;
        setTimeLeft(30);
        // trigger resend API here in integration
    };

  return (
      <div style={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "#F4F6F9",
          padding: "24px"
      }}>
          <div style={{ width: "100%", maxWidth: 520 }}>
              {/* Page heading */}
              <h1 style={{ textAlign: "center", margin: 0, fontSize: "2rem", lineHeight: 1.2 }}>
                  Sign in to your account
              </h1>
              <p style={{ textAlign: "center", marginTop: 8, color: "#6B7280" }}>
                  Welcome back! Please enter your credentials
              </p>

              {/* Card */}
              <div style={{
                  marginTop: 24,
                  background: "#fff",
                  borderRadius: 8,
                  boxShadow: "0 1px 2px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.1)"
              }}>
                  <div style={{ padding: 24 }}>
                      {/* “Tabs” using two Buttons so you don’t need extra APIs */}
                      <div role="tablist" aria-label="Role" style={{ display: "flex", gap: 12, marginBottom: 16 }}>
                          <Button
                              type="button"
                              variant={role === "JUDGE" ? "primary" : "secondary"}
                              onClick={() => setRole("JUDGE")}
                              label="Judge/ Court Staff"
                          />
                          <Button
                              type="button"
                              variant={role === "ADV" ? "primary" : "secondary"}
                              onClick={() => setRole("ADV")}
                              label="Advocate/ Litigant"
                          />
                      </div>

                      {/* Phone field */}
                      <form onSubmit={handleSubmit} noValidate>
                          <TextInput
                              name="phone"
                              label="Phone No"
                              type="tel"
                              value={phone}
                              // TextInput in ui-components supports prefix/suffix variants; most versions accept `prefix`
                              prefix="+91"
                              placeholder="Enter 10-digit phone"
                              onChange={(e) => setPhone(e.target.value.replace(/\D/g, ""))}
                              error={phone.length > 0 && !isValidPhone ? "Enter a valid 10-digit number" : ""}
                          />

                          <div style={{ marginTop: 16 }}>
                              <Button
                                  type="submit"
                                  label="Sign In"
                                  // full-width button: if your Button doesn’t accept fullWidth, just use style
                                  style={{ width: "100%" }}
                                  disabled={!isValidPhone}
                              />
                          </div>
                      </form>

                      {/* Footer links */}
                      <div style={{ marginTop: 12, textAlign: "center", fontSize: 14 }}>
                          Don’t have an account?{" "}
                          <a href="/digit-ui/employee/register">Register here</a>
                      </div>
                  </div>
              </div>

              {/* Optional footer links area */}
              <div style={{ marginTop: 16, textAlign: "center", fontSize: 14, color: "#6B7280" }}>
                  Want to know more?{" "}
                  <a href="https://core.digit.org/" target="_blank" rel="noreferrer">View our SOP</a>{" "}
                  or{" "}
                  <a href="https://core.digit.org/" target="_blank" rel="noreferrer">Watch the platform video</a>
              </div>
          </div>

          {showOtp && (
              <PopUp className="digit-otp-popup">
                  <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                      <div style={{ width: 520, background: "#FFFFFF", borderRadius: 8, boxShadow: "0 10px 25px rgba(0,0,0,0.2)" }}>
                          <div style={{ position: "relative", padding: 24 }}>
                              <button onClick={() => setShowOtp(false)} aria-label="Close" style={{ position: "absolute", right: 12, top: 12, background: "#2E5B66", color: "#fff", border: 0, width: 28, height: 28, borderRadius: 4, cursor: "pointer" }}>×</button>
                              <h2 style={{ margin: 0, fontSize: "1.25rem" }}>Verify your Mobile Number</h2>
                              <CardText style={{ marginTop: 8 }}>Enter the OTP sent to +91******{phone.slice(-4)}.</CardText>

                              <div style={{ marginTop: 16 }}>
                                  <OTPInput length={6} onChange={setOtp} value={otp} />
                              </div>

                              <div style={{ marginTop: 12, display: "flex", alignItems: "center", gap: 12 }}>
                                  <CardText style={{ margin: 0 }}>Request a new OTP in {`0:${String(timeLeft).padStart(2, "0")}`}</CardText>
                                  <button onClick={handleResend} disabled={!canResend} style={{ background: "transparent", border: 0, color: canResend ? "#0B4B66" : "#C5C5C5", cursor: canResend ? "pointer" : "not-allowed", padding: 0 }}>Resend OTP</button>
                              </div>

                              <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 16 }}>
                                  <Button type="button" label="Verify" variant="primary" disabled={otp.length !== 6} onClick={handleVerifyOtp} />
                              </div>
                          </div>
                      </div>
                  </div>
              </PopUp>
          )}
      </div>
  );
};

export default Auth;


