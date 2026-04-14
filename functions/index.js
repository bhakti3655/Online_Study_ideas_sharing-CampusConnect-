const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

// Email configuration
const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: "campusconnect2801@gmail.com",
        pass: "gbyu jnug qwws tmvw" // Ensure this App Password is valid for this account
    }
});

exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
    const email = user.email;
    const displayName = user.displayName || "New User";

    const mailOptions = {
        from: '"Campus Connect Team" <campusconnect2801@gmail.com>',
        to: email,
        subject: "Welcome to Campus Connect!",
        text: `Hello ${displayName},\n\nWelcome to Campus Connect! We are thrilled to have you join our community.\n\nBest Regards,\nCampus Connect Team`
    };

    return transporter.sendMail(mailOptions)
        .then(() => {
            console.log("Welcome email sent to:", email);
            return null;
        })
        .catch((error) => {
            console.error("Error sending welcome email:", error);
            return null;
        });
});