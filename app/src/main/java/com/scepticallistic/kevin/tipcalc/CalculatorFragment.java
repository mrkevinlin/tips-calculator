package com.scepticallistic.kevin.tipcalc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class CalculatorFragment extends Fragment implements AdapterView.OnItemSelectedListener, PercentDialogFragment.PercentDialogListener {
    private static final String LOG_TAG = CalculatorFragment.class.getSimpleName();
    private static final ArrayList<String> percents = new ArrayList<>();
    private static final ArrayList<String> defPercents = new ArrayList<>();
    public Activity main;
    public View rootView, scroll_view, calculator_card, sale_text, tip_text, total_text, fab_plus, split_card, people_count, split_tip, split_total;
    public Spinner spinner;
    public ArrayAdapter<String> adapter;
    public double sale, percent, tip, total, splitTip, splitTotal;
    public int people, defPeople, percent_array_length, spinnerPosition;
    public String prefUnitKey, prefPeopleKey, prefPercentsKey, unitSymbol, defaultPercentString, addPercentString;
    boolean recalculate = true;
    boolean rounding = false;

    public CalculatorFragment() {

    }

    private void setPeoplePreference(SharedPreferences sp) {
        defPeople = Integer.parseInt(sp.getString(
                main.getString(R.string.pref_people_key),
                main.getString(R.string.pref_people_default)));
        people = defPeople;
    }

    public void setUnitPreference(SharedPreferences sp) {
        unitSymbol = sp.getString(
                main.getString(R.string.pref_unit_key),
                main.getString(R.string.pref_unit_default));
        ((TextView) rootView.findViewById(R.id.dollar1)).setText(unitSymbol);
        ((TextView) rootView.findViewById(R.id.dollar2)).setText(unitSymbol);
        ((TextView) rootView.findViewById(R.id.dollar3)).setText(unitSymbol);
    }

    private void setSpinnerPreferences(SharedPreferences sp) {
        defPercents.clear();
        percents.clear();
        defaultPercentString = sp.getString(
                main.getString(R.string.pref_percents_key),
                main.getString(R.string.pref_percents_default));

        // If user did not enter in any valid numbers, resort to default.
        if (!defaultPercentString.matches(".*\\d.*")) {
            defaultPercentString = main.getString(R.string.pref_percents_default);
        }

        while (defaultPercentString.contains(",") && defaultPercentString.matches(".*\\d.*")) {

            addPercentString = defaultPercentString.substring(0, defaultPercentString.indexOf(","));

            if (addPercentString.matches("\\d+")) {
                defPercents.add(addPercentString + "%");
            }
            defaultPercentString = defaultPercentString.substring(defaultPercentString.indexOf(",") + 1);

        }
        
        // Adding in the last number that is not bordered by a comma.
        if (!defaultPercentString.isEmpty() && defaultPercentString.matches("\\d+")) {
            defPercents.add(defaultPercentString + "%");
        }
        defPercents.add("Custom");
//        Collections.sort(defPercents);

        percent_array_length = defPercents.size();
        percents.addAll(defPercents);
        spinner.setSelection(0);
        spinner.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        main = getActivity();

        rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

        prefUnitKey = main.getString(R.string.pref_unit_key);
        prefPeopleKey = main.getString(R.string.pref_people_key);
        prefPercentsKey = main.getString(R.string.pref_percents_key);

        spinner = (Spinner) rootView.findViewById(R.id.percent_amount_spinner);
        adapter = new ArrayAdapter<>(main, R.layout.spinner_item, percents);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main);
        setUnitPreference(preferences);
        setSpinnerPreferences(preferences);
        setPeoplePreference(preferences);

        SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(prefPeopleKey)) {
                    setPeoplePreference(sharedPreferences);
                } else if (key.equals(prefUnitKey)) {
                    setUnitPreference(sharedPreferences);
                } else if (key.equals(prefPercentsKey)) {
                    setSpinnerPreferences(sharedPreferences);
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(prefListener);

        scroll_view = rootView.findViewById(R.id.scroll_view);
        calculator_card = rootView.findViewById(R.id.calculator_card);
        sale_text = rootView.findViewById(R.id.sale_amount);
        tip_text = rootView.findViewById(R.id.tip_amount);
        total_text = rootView.findViewById(R.id.total_amount);
        split_card = rootView.findViewById(R.id.split_card);
        fab_plus = rootView.findViewById(R.id.fab_plus);
        people_count = rootView.findViewById(R.id.people_count);
        split_tip = rootView.findViewById(R.id.split_tip);
        split_total = rootView.findViewById(R.id.split_total);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setSelection(0);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        setCalcListeners();

        Animation rotateX = AnimationUtils.loadAnimation(main, R.anim.fab_rotate_to_x);
        Animation rotatePlus = AnimationUtils.loadAnimation(main, R.anim.fab_rotate_to_plus);

        split_card.animate().alpha(0f).translationY(-100f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeFABPlusOutline(fab_plus);
        }

        setButtons(rootView, rotatePlus, rotateX);

        return rootView;
    }

    private void setCalcListeners() {

        final TextView sale_view = (TextView) sale_text;
        sale_view.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        sale = Double.parseDouble(((TextView) sale_text).getText().toString());
                    } else {
                        sale = 0;
                    }
                    saleChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        sale_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
                    sale_view.clearFocus();
                    spinner.requestFocus();
                    spinner.performClick();
                }
                return true;
            }

        });

        ((TextView) tip_text).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        tip = Double.parseDouble(((TextView) tip_text).getText().toString());
                    } else {
                        tip = 0;
                    }
                    tipChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ((TextView) total_text).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        total = Double.parseDouble(((TextView) total_text).getText().toString());
                    } else {
                        total = 0;
                    }
                    totalChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void setButtons(View rootView, final Animation rotatePlus, final Animation rotateX) {

        final Button clear_button = (Button) rootView.findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recalculate = false;
                ((ScrollView) scroll_view).smoothScrollTo(0, 0);

                ((TextView) sale_text).setText("");
                ((TextView) tip_text).setText("");
                ((TextView) total_text).setText("");
                calcSplits();
                sale = 0;
                resetSpinner();
                tip = 0;
                total = 0;
                people = defPeople;
                recalculate = true;
                if (split_card.isShown())
                    hideCard(rotatePlus);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    clearReveal(clear_button);
                }
            }
        });

        Button round_button = (Button) rootView.findViewById(R.id.round_button);
        round_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (total != 0) {
                    ((TextView) total_text).setText(String.format("%.2f", (double) Math.round(total)));
                    rounding = true;
                    if (split_card.isShown()) {
                        calcSplits();
                    }
                }
            }
        });


        Button split_button = (Button) rootView.findViewById(R.id.split_button);
        split_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (split_card.isShown()) {
                    hideCard(rotatePlus);
                } else {
                    showCard(rotateX);
                }
            }
        });

        Button add_people = (Button) rootView.findViewById(R.id.add_people);
        Button take_people = (Button) rootView.findViewById(R.id.take_people);

        add_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                people++;
                setPeople();
                calcSplits();
            }
        });

        take_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (people > 2) {
                    people--;
                } else {
                    hideCard(rotatePlus);
                    people = defPeople;
                }
                setPeople();
                calcSplits();
            }
        });
    }

    @TargetApi(21) // Remove the drop shadow from the + drawable
    private void removeFABPlusOutline(View plusView) {
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                view.setOutlineProvider(null);
            }
        };
        plusView.setOutlineProvider(viewOutlineProvider);
    }

    @TargetApi(21) // Material ripple to clear the card
    private void clearReveal(Button source) {
        LinearLayout.LayoutParams shift = (LinearLayout.LayoutParams) calculator_card.getLayoutParams();

        int centerX = ((source.getLeft() + source.getRight()) / 2)
                + shift.leftMargin;
        int centerY = (calculator_card.getBottom() - (source.getHeight() / 2))
                - (shift.topMargin + calculator_card.getPaddingBottom());
        int radius = (int) Math.sqrt(Math.pow(calculator_card.getWidth(), 2) + Math.pow(calculator_card.getHeight(), 2));

        Animator reveal = ViewAnimationUtils.createCircularReveal(
                calculator_card,
                centerX,
                centerY,
                0,
                radius);
        reveal.setDuration(400);
        reveal.start();
    }

    private void showCard(Animation animation) {
        fab_plus.startAnimation(animation);
        split_card.setVisibility(View.VISIBLE);
        split_card.animate().alpha(1f).translationY(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ScrollView) scroll_view).smoothScrollTo(0, split_card.getBottom());
            }
        });
        setPeople();
        calcSplits();
    }

    private void hideCard(Animation animation) {
        fab_plus.startAnimation(animation);
        split_card.animate().alpha(0f).translationY(-100f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                ((ScrollView) scroll_view).smoothScrollTo(0, 0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                split_card.setVisibility(View.GONE);
            }
        });
    }

    public void addPercentToSpinner(double p) {
        rounding = true;
        spinner.setAdapter(adapter);
        if (p == 0) {
            spinner.setSelection(spinnerPosition);
        } else {
            if (percents.size() > percent_array_length) {
                percents.remove(percents.size() - 1);
            }
            percents.add(String.format("%.1f", p) + "%");
            spinner.setSelection(percents.size() - 1);
            spinnerPosition = percents.size() - 1;
        }
        rounding = false;
    }

    private void showPercentDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        PercentDialogFragment percentDialog = new PercentDialogFragment();
        percentDialog.setTargetFragment(this, 0);
        percentDialog.show(fm, "custom_percent_dialog");
    }

    public void resetSpinner() {
        percents.clear();
        percents.addAll(defPercents);
        spinner.setSelection(0);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (recalculate && !rounding) {
            String percent_number = parent.getItemAtPosition(position).toString();
            percent_number = percent_number.substring(0, percent_number.length() - 1);
            try {
                percent = Double.parseDouble(percent_number);
                percentChanged();
                spinnerPosition = position;
                // Remove messy recalculated or custom percentages upon selection of default ones.
                if (percents.size() > percent_array_length) {
                    percents.remove(percents.size()-1);
                }
            } catch (NumberFormatException e) {
                if (percent_number.equals("Custo")) {
                    showPercentDialog();
                }
            }
        }
        rounding = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void saleChanged() {
        if (percent != 0 && recalculate) {
            recalculate = false;
            tip = sale * (percent / 100);
            total = sale + tip;

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            ((TextView) total_text).setText(String.format("%.2f", total));

            if (split_card.isShown()) {
                calcSplits();
            }
            recalculate = true;
        }

    }

    private void percentChanged() {
        if (sale != 0 && recalculate) {
            recalculate = false;
            tip = sale * (percent / 100);
            total = sale + tip;

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            ((TextView) total_text).setText(String.format("%.2f", total));

            if (split_card.isShown()) {
                calcSplits();
            }
            recalculate = true;
        }
    }

    private void tipChanged() {
        if (sale != 0 && recalculate) {
            recalculate = false;
            percent = (tip / sale) * 100;
            total = sale + tip;


            addPercentToSpinner(percent);
            ((TextView) total_text).setText(String.format("%.2f", total));

            if (split_card.isShown()) {
                calcSplits();
            }
            recalculate = true;
        }
    }

    private void totalChanged() {
        if (sale != 0 && recalculate && total > sale) {
            recalculate = false;
            tip = total - sale;
            percent = (tip / sale) * 100;

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            addPercentToSpinner(percent);

            recalculate = true;
        }

        if (split_card.isShown()) {
            calcSplits();
        }

    }

    private void setPeople() {
        ((TextView) people_count).setText(Integer.toString(people) + getString(R.string.people));
    }

    private void calcSplits() {
        splitTip = tip / people;
        splitTotal = total / people;

        ((TextView) split_tip).setText(unitSymbol + String.format("%.2f", splitTip));
        ((TextView) split_total).setText(unitSymbol + String.format("%.2f", splitTotal));
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) main.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(main.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        percents.clear();
    }
}