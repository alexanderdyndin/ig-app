package com.intergroupapplication.presentation.feature.creategroup.view

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.setViewErrorState
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.creategroup.presenter.CreateGroupPresenter
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_group_create.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import javax.inject.Inject

class CreateGroupFragment : BaseFragment(), CreateGroupView, Validator.ValidationListener {

    companion object {
        fun getIntent(context: Context?) = Intent(context, CreateGroupFragment::class.java)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateGroupPresenter

    @ProvidePresenter
    fun providePresenter(): CreateGroupPresenter = presenter

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var groupName: EditText

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate

    private var age: String = "12"
    private var subjects: MutableList<String> = mutableListOf<String>()

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_group_create

    override fun getSnackBarCoordinator(): CoordinatorLayout = createGroupCoordinator

    override fun viewCreated() {
        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter<String>(
                requireContext(), R.layout.item_autocomplete, R.id.autoCompleteItem, countries
        )
        val imm: InputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        autoCompleteTextViewCountry.setAdapter(adapter)
        autoCompleteTextViewCountry.threshold = 2

        groupCreate__desc.setOnClickListener {
            it.visibility = View.GONE
            groupCreate__descContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            groupCreate__descContainer.performClick()
        }

        groupCreate__descEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                groupCreate__desc.visibility = View.VISIBLE
                groupCreate__descContainer.visibility = View.GONE
            }
        }

        groupCreate__rule.setOnClickListener {
            it.visibility = View.GONE
            groupCreate__ruleContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            groupCreate__ruleContainer.performClick()
        }

        groupCreate__ruleEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                groupCreate__rule.visibility = View.VISIBLE
                groupCreate__ruleContainer.visibility = View.GONE
            }
        }
        groupCreate__ruleContainer.setOnClickListener {
            groupCreate__ruleEdit.requestFocus()
            showKeyboard(imm)
        }
        groupCreate__descContainer.setOnClickListener {
            groupCreate__descEdit.requestFocus()
            showKeyboard(imm)
        }


        groupCreate__radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.groupCreate_btnOpen -> {
                    groupCreate_btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_btn_act), null, null, null)
                    groupCreate_btnOpen.setTextColor(requireContext().getColor(R.color.ActiveText))
                    groupCreate_btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_btn), null, null, null)
                    groupCreate_btnClose.setTextColor(requireContext().getColor(R.color.colorPink))
                }
                R.id.groupCreate_btnClose -> {
                    groupCreate_btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_btn), null, null, null)
                    groupCreate_btnOpen.setTextColor(requireContext().getColor(R.color.colorAccent))
                    groupCreate_btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_btn_act), null, null, null)
                    groupCreate_btnClose.setTextColor(requireContext().getColor(R.color.ActiveText))
                }
            }
        }

        groupCreate__subject_btn.setOnClickListener {
            val keyword: String = groupCreate__subject.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(requireContext(), R.string.empty_subject_error, Toast.LENGTH_SHORT).show()
            } else if (subjects.size>=5) {
                Toast.makeText(requireContext(), R.string.full_subject_error, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val inflater = LayoutInflater.from(requireContext())
                    val newChip = inflater.inflate(R.layout.layout_chip_entry, chipGroupSubject, false) as Chip
                    newChip.text = keyword
                    subjects.add(keyword)
                    chipGroupSubject.addView(newChip)
                    newChip.setOnCloseIconClickListener {
                        subjects.remove((it as Chip).text)
                        chipGroupSubject.removeView(it)
                        groupCreate__lineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                    }
                    groupCreate__subject.setText("")
                    groupCreate__lineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }



        groupCreate__checkAge.setOnCheckedChangeListener { _, i ->
            groupCreate__lineAge.setBackgroundResource(R.drawable.line_input_act)
            when (i) {
                R.id.groupCreate__btnAge12 -> age = "12"
                R.id.groupCreate__btnAge16 -> age = "16"
                R.id.groupCreate__btnAge18 -> age = "18"
            }
        }



        groupCreate__ageHelp.setOnClickListener {
            Toast.makeText(requireContext(), "12+\n16+\n18+", Toast.LENGTH_LONG).show()
        }
        groupName = requireView().findViewById(R.id.groupCreate_title)
        createGroup.setOnClickListener {
            //todo write rules for validator
            validator.validate()
        }
        groupCreate__addAvatar.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
        }
        toolbarBackAction.setOnClickListener {
//            setResult(Activity.RESULT_OK)
//            finish()
                findNavController().popBackStack()
        }
        RxTextView.afterTextChangeEvents(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.focusChanges(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }

        groupAvatarHolder.imageLoaderDelegate = imageLoaderDelegate
        checkBoxAgreement.setOnCheckedChangeListener { _, b ->
            if (b) {
                createGroup.setBackgroundResource(R.drawable.selector_btn_create)
                createGroup.setTextColor(resources.getColor(R.color.ActiveText, requireContext().theme))
                createGroup.isEnabled = true
            } else {
                createGroup.setBackgroundResource(R.drawable.btn_main)
                createGroup.setTextColor(resources.getColor(R.color.colorTextBtnNoActive, requireContext().theme))
                createGroup.isEnabled = false
            }
        }

        groupName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    groupCreate__lineOpenClose.setBackgroundResource(R.drawable.line_openclose_act)
                    groupCreate__lineAge.setBackgroundResource(R.drawable.line_input_act)
                } else {
                    groupCreate__lineOpenClose.setBackgroundResource(R.drawable.line_openclose)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCountry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput2.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewLang.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput3.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        groupCreate__descEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput5.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        groupCreate__ruleEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput6.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })


        groupCreate_btnOpen.isChecked = true
        groupCreate__btnAge12.isChecked = true
    }

    override fun showImageUploadingStarted(path: String) {
       groupAvatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded() {
        groupAvatarHolder.showImageUploaded()
        groupCreate_line_r.setBackgroundResource(R.drawable.line_addava_act)
    }

    override fun showImageUploadingProgress(progress: Float) {
        groupAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
        groupAvatarHolder.showImageUploadingError()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(requireContext())
            if (view is AppCompatEditText) {
                groupName.error = message
                setViewErrorState(view)
            }
        }
    }

    override fun onValidationSucceeded() {
        if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            presenter.createGroup(groupName.text.toString().trim(),
                    groupCreate__desc.text.toString().trim(), "no theme",
                    groupCreate__rule.text.toString().trim(), groupCreate_btnClose.isChecked, age)
        } else {
            dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
        }
    }

    override fun goToGroupScreen(id: String) {
        val data = bundleOf("groupId" to id)
        findNavController().navigate(R.id.action_newsFragment2_to_groupActivity, data)
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            createGroup.hide()
            progressBar.show()
        } else {
            progressBar.hide()
            createGroup.show()
        }
    }

    private fun loadFromCamera() {
        presenter.takePhotoFromCamera()
    }

    private fun loadFromGallery() {
        presenter.takePhotoFromGallery()
    }


    fun View.changeSeparatorBackground(active: Boolean) {
        if (active) {
            setBackgroundResource(R.drawable.line_input_act)
        } else {
            setBackgroundResource(R.drawable.line_input)
        }
    }

    private fun hideKeyboard(imm: InputMethodManager) {
        val view: View? = this.requireActivity().currentFocus
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard(imm: InputMethodManager) {
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

}
