package com.example.campusconnect

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.campusconnect.databinding.FragmentOtpVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpVerificationFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var phoneNumber: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        phoneNumber = arguments?.getString("phoneNumber")

        if (phoneNumber != null) {
            binding.tvOtpDesc.text = "Enter the 6-digit code sent to $phoneNumber"
            startPhoneNumberVerification(phoneNumber!!)
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.length == 6) {
                verifyOtp(code)
            } else {
                Toast.makeText(requireContext(), "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvResend.setOnClickListener {
            if (phoneNumber != null) {
                resendVerificationCode(phoneNumber!!, resendToken)
            }
        }

        startTimer()
    }

    private fun startTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "00:${millisUntilFinished / 1000}"
                binding.tvResend.isEnabled = false
                binding.tvResend.alpha = 0.5f
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                binding.tvResend.isEnabled = true
                binding.tvResend.alpha = 1.0f
            }
        }.start()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification if possible
            val code = credential.smsCode
            if (code != null) {
                binding.etOtp.setText(code)
                verifyOtp(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(requireContext(), "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@OtpVerificationFragment.verificationId = verificationId
            this@OtpVerificationFragment.resendToken = token
            Toast.makeText(requireContext(), "OTP Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
        
        if (token != null) {
            optionsBuilder.setForceResendingToken(token)
        }
        
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        startTimer()
    }

    private fun verifyOtp(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        // Since we are resetting password, we don't necessarily sign in here.
        // Usually, for password reset via phone, you verify the phone and then let them change password.
        // For simplicity in this flow, we navigate to Reset Password screen.
        findNavController().navigate(R.id.action_otpVerificationFragment_to_resetPasswordFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}