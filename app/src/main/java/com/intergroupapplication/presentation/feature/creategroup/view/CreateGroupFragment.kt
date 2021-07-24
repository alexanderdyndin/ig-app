package com.intergroupapplication.presentation.feature.creategroup.view

import android.content.Context.INPUT_METHOD_SERVICE
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentGroupCreateBinding
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.setViewErrorState
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.creategroup.presenter.CreateGroupPresenter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListFragment.Companion.CREATED_GROUP_ID
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.disposables.Disposable
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class CreateGroupFragment : BaseFragment(), CreateGroupView, Validator.ValidationListener {

    private val viewBinding by viewBinding(FragmentGroupCreateBinding::bind)

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
    private var subjects: MutableList<String> = mutableListOf()

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_group_create

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.createGroupCoordinator

    private lateinit var autoCompleteTextViewCountry: AutoCompleteTextView
    private lateinit var autoCompleteTextViewCity: AutoCompleteTextView
    private lateinit var autoCompleteTextViewLang: AutoCompleteTextView
    private lateinit var toolbarTittle: TextView

    override fun viewCreated() {
        autoCompleteTextViewCountry = viewBinding.autoCompleteTextViewCountry
        autoCompleteTextViewCity = viewBinding.autoCompleteTextViewCity
        autoCompleteTextViewLang = viewBinding.autoCompleteTextViewLang
        toolbarTittle = viewBinding.navigationToolbar.toolbarTittle

        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter<String>(
                requireContext(), R.layout.item_autocomplete, R.id.autoCompleteItem, countries
        )
        val imm: InputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        autoCompleteTextViewCountry.setAdapter(adapter)
        autoCompleteTextViewCountry.threshold = 2
        toolbarTittle.text = getString(R.string.create_group)
        viewBinding.groupCreateDesc.setOnClickListener {
            it.visibility = View.GONE
            viewBinding.groupCreateDescContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            viewBinding.groupCreateDescContainer.performClick()
        }

        viewBinding.groupCreateDescEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                viewBinding.groupCreateDesc.visibility = View.VISIBLE
                viewBinding.groupCreateDescContainer.visibility = View.GONE
            }
        }

        viewBinding.groupCreateRule.setOnClickListener {
            it.visibility = View.GONE
            viewBinding.groupCreateRuleContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            viewBinding.groupCreateRuleContainer.performClick()
        }

        viewBinding.groupCreateRuleEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                viewBinding.groupCreateRule.visibility = View.VISIBLE
                viewBinding.groupCreateRuleContainer.visibility = View.GONE
            }
        }
        viewBinding.groupCreateRuleContainer.setOnClickListener {
            viewBinding.groupCreateRuleEdit.requestFocus()
            showKeyboard(imm)
        }
        viewBinding.groupCreateDescContainer.setOnClickListener {
            viewBinding.groupCreateDescEdit.requestFocus()
            showKeyboard(imm)
        }


        viewBinding.groupCreateRadioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.groupCreate_btnOpen -> {
                    viewBinding.groupCreateBtnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_btn_act), null, null, null)
                    viewBinding.groupCreateBtnOpen.setTextColor(requireContext().getColor(R.color.ActiveText))
                    viewBinding.groupCreateBtnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_btn), null, null, null)
                    viewBinding.groupCreateBtnClose.setTextColor(requireContext().getColor(R.color.colorPink))
                }
                R.id.groupCreate_btnClose -> {
                    viewBinding.groupCreateBtnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_btn), null, null, null)
                    viewBinding.groupCreateBtnOpen.setTextColor(requireContext().getColor(R.color.colorAccent))
                    viewBinding.groupCreateBtnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_btn_act), null, null, null)
                    viewBinding.groupCreateBtnClose.setTextColor(requireContext().getColor(R.color.ActiveText))
                }
            }
        }

        viewBinding.groupCreateSubjectBtn.setOnClickListener {
            val keyword: String = viewBinding.groupCreateSubject.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(requireContext(), R.string.empty_subject_error, Toast.LENGTH_SHORT).show()
            } else if (subjects.size>=5) {
                Toast.makeText(requireContext(), R.string.full_subject_error, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val inflater = LayoutInflater.from(requireContext())
                    val newChip = inflater.inflate(R.layout.layout_chip_entry, viewBinding.chipGroupSubject, false) as Chip
                    newChip.text = keyword
                    subjects.add(keyword)
                    viewBinding.chipGroupSubject.addView(newChip)
                    newChip.setOnCloseIconClickListener {
                        subjects.remove((it as Chip).text)
                        viewBinding.chipGroupSubject.removeView(it)
                        viewBinding.groupCreateLineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                    }
                    viewBinding.groupCreateSubject.setText("")
                    viewBinding.groupCreateLineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewBinding.groupCreateCheckAge.setOnCheckedChangeListener { _, i ->
            viewBinding.groupCreateLineAge.setBackgroundResource(R.drawable.line_input_act)
            when (i) {
                R.id.groupCreate__btnAge12 -> age = "12"
                R.id.groupCreate__btnAge16 -> age = "16"
                R.id.groupCreate__btnAge18 -> age = "18"
            }
        }



        viewBinding.groupCreateAgeHelp.setOnClickListener {
            Toast.makeText(requireContext(), "12+\n16+\n18+", Toast.LENGTH_LONG).show()
        }
        groupName = requireView().findViewById(R.id.groupCreate_title)
        viewBinding.createGroup.setOnClickListener {
            validator.validate()
        }
        viewBinding.groupCreateAddAvatar.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
        }
        viewBinding.navigationToolbar.toolbarBackAction.setOnClickListener {
            findNavController().popBackStack()
        }
        RxTextView.afterTextChangeEvents(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.focusChanges(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }

        viewBinding.groupAvatarHolder.imageLoaderDelegate = imageLoaderDelegate
        viewBinding.checkBoxAgreement.setOnCheckedChangeListener { _, b ->
            if (b) {
                viewBinding.createGroup.setBackgroundResource(R.drawable.selector_btn_create)
                viewBinding.createGroup.setTextColor(resources.getColor(R.color.ActiveText, requireContext().theme))
                viewBinding.createGroup.isEnabled = true
            } else {
                viewBinding.createGroup.setBackgroundResource(R.drawable.btn_main)
                viewBinding.createGroup.setTextColor(resources.getColor(R.color.colorTextBtnNoActive, requireContext().theme))
                viewBinding.createGroup.isEnabled = false
            }
        }

        groupName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    viewBinding.groupCreateLineOpenClose.setBackgroundResource(R.drawable.line_openclose_act)
                    viewBinding.groupCreateLineAge.setBackgroundResource(R.drawable.line_input_act)
                } else {
                    viewBinding.groupCreateLineOpenClose.setBackgroundResource(R.drawable.line_openclose)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCountry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewBinding.groupCreateLineInput.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewBinding.groupCreateLineInput2.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewLang.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewBinding.groupCreateLineInput3.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        viewBinding.groupCreateDescEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewBinding.groupCreateLineInput5.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        viewBinding.groupCreateRuleEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewBinding.groupCreateLineInput6.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })


        viewBinding.groupCreateBtnOpen.isChecked = true
        viewBinding.groupCreateBtnAge12.isChecked = true
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        viewBinding.groupAvatarHolder.showImageUploadingStarted(chooseMedia)
    }



    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        viewBinding.groupAvatarHolder.showImageUploaded(chooseMedia)
        viewBinding.groupCreateLineR.setBackgroundResource(R.drawable.line_addava_act)
    }


    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        viewBinding.groupAvatarHolder.showImageUploadingProgress(progress,chooseMedia)
    }


    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        viewBinding.groupAvatarHolder.showImageUploadingError(chooseMedia)
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(requireContext())
            context?.let {context->
                dialogDelegate.showErrorSnackBar(
                        context.getString(R.string.group_name_is_not_empty))
            }
            if (view is AppCompatEditText) {
                groupName.error = message
                setViewErrorState(view)
            }
        }
    }

    override fun onValidationSucceeded() {
        if (viewBinding.groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
            viewBinding.groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
            viewBinding.groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            presenter.createGroup(groupName.text.toString().trim(),
                viewBinding.groupCreateDesc.text.toString().trim(), "no theme",
                    viewBinding.groupCreateRule.text.toString().trim(),
                    viewBinding.groupCreateBtnClose.isChecked, age)
        } else {
            dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
        }
    }

    override fun goToGroupScreen(id: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(CREATED_GROUP_ID, id)
        findNavController().popBackStack()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            viewBinding.createGroup.hide()
            viewBinding.loader.progressBar.show()
        } else {
            viewBinding.loader.progressBar.hide()
            viewBinding.createGroup.show()
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
