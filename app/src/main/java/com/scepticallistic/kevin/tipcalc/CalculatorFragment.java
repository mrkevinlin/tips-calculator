package com.scepticallistic.kevin.tipcalc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CalculatorFragment extends Fragment {

    private static final String LOG_TAG = CalculatorFragment.class.getSimpleName();
    private static final Integer[] percents = new Integer[]{15, 18, 20};
    public View scroll_view, calculator_card, sale_text, percent_text, tip_text, total_text, fab_plus, split_card, people_count, split_tip, split_total;
    public double sale, percent, tip, total, splitTip, splitTotal;
    public int people = 2;

    boolean recalculate = true;

    public CalculatorFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

//        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getActivity(),
//                android.R.layout.simple_dropdown_item_1line, percents);
//        final AutoCompleteTextView autoTextView = (AutoCompleteTextView)
//                rootView.findViewById(R.id.percent_amount);
//        autoTextView.setAdapter(adapter);

        //TODO: figure out why after entering in values, dropdown no longer shows
//        autoTextView.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent event){
//                autoTextView.showDropDown();
//                return false;
//            }
//        });

        scroll_view = rootView.findViewById(R.id.scroll_view);
        calculator_card = rootView.findViewById(R.id.calculator_card);

        sale_text = rootView.findViewById(R.id.sale_amount);
        percent_text = rootView.findViewById(R.id.percent_amount);
        tip_text = rootView.findViewById(R.id.tip_amount);
        total_text = rootView.findViewById(R.id.total_amount);

        split_card = rootView.findViewById(R.id.split_card);

        fab_plus = rootView.findViewById(R.id.fab_plus);

        people_count = rootView.findViewById(R.id.people_count);
        split_tip = rootView.findViewById(R.id.split_tip);
        split_total = rootView.findViewById(R.id.split_total);

        ((EditText) sale_text).setSelection(0);

//        ArrayList<View> calculatorElements = new ArrayList<>();
//        calculatorElements.add(sale_text);
//        calculatorElements.add(percent_text);
//        calculatorElements.add(tip_text);
//        calculatorElements.add(total_text);

        //setHideKeyboard(rootView, calculatorElements);

        setCalcListeners();

        final Animation rotateX = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_rotate_to_x);
        final Animation rotatePlus = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_rotate_to_plus);
        split_card.animate().alpha(0f).translationY(-100f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeFABPlusOutline(fab_plus);
        }

        final Button clear_button = (Button) rootView.findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recalculate = false;
                ((TextView) sale_text).setText("");
                ((TextView) percent_text).setText("");
                ((TextView) tip_text).setText("");
                ((TextView) total_text).setText("");
                calcSplits();
                sale = 0;
                percent = 0;
                tip = 0;
                total = 0;
                recalculate = true;
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
                    people = 2;
                }
                setPeople();
                calcSplits();
            }
        });

        return rootView;
    }

    @TargetApi(21)
    private void clearReveal(Button source) {
        LinearLayout.LayoutParams shift = (LinearLayout.LayoutParams) calculator_card.getLayoutParams();

        int centerX = ((source.getLeft() + source.getRight()) / 2)
                + (int) shift.leftMargin;
        int centerY = (calculator_card.getBottom() - (source.getHeight() / 2))
                - (int) (shift.topMargin + calculator_card.getPaddingBottom());
        int radius = (int) Math.sqrt(Math.pow(calculator_card.getWidth(), 2) + Math.pow(calculator_card.getHeight(), 2));

        Log.d(LOG_TAG, Integer.toString(radius));
        Log.d(LOG_TAG, Integer.toString(Math.max(calculator_card.getWidth(), calculator_card.getHeight())));

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

    @TargetApi(21)
    private void removeFABPlusOutline(View plusView) {
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                view.setOutlineProvider(null);
            }
        };
        plusView.setOutlineProvider(viewOutlineProvider);
    }

    private void setCalcListeners() {

//        for (int i = 0; i < calcElems.size(); i++) {
//            ((TextView)calcElems.get(i)).addTextChangedListener(new TextWatcher() {
//                @Override
//                public void afterTextChanged(Editable s) {
//                    vals[i] = Double.parseDouble(((TextView)calcElems.get(i)).getText().toString());
//                }
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            });
//        }

        ((TextView) sale_text).addTextChangedListener(new TextWatcher() {

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

        ((TextView) percent_text).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        percent = Double.parseDouble(((TextView) percent_text).getText().toString());
                    } else {
                        percent = 0;
                    }
                    percentChanged();
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

        ((TextView) people_count).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() != 0) {
                        people = Integer.parseInt(((TextView) people_count).getText().toString());
                    } else {
                        people = 2;
                    }
                    totalChanged();
                } catch (NumberFormatException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
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

            ((TextView) percent_text).setText(String.format("%.1f", percent));
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
            ((TextView) percent_text).setText(String.format("%.1f", percent));

            recalculate = true;
        }

        if (split_card.isShown()) {
            calcSplits();
        }

    }

    private void calcSplits() {
        splitTip = tip / people;
        splitTotal = total / people;

        ((TextView) split_tip).setText("$" + String.format("%.2f", splitTip));
        ((TextView) split_total).setText("$" + String.format("%.2f", splitTotal));
    }

    private void setPeople() {
        ((TextView) people_count).setText(Integer.toString(people));
    }

//    private void setHideKeyboard(View root, ArrayList<View> calcElems) {
//
//        if (!calcElems.isEmpty()) {
//            for (int i = 0; i < calcElems.size(); i++) {
//                calcElems.get(i).setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        if (hasFocus) {
//                            showKeyboard(v);
//                        } else if (!hasFocus) {
//                            hideKeyboard(v);
//                        }
//                    }
//                });
//            }
//        }
//    }
//
//    public void hideKeyboard(View view) {
//        InputMethodManager inputMethodManager =
//                (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
//    }
//
//    public void showKeyboard(View view) {
//        InputMethodManager inputMethodManager =
//                (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.showSoftInput(view, 0);
//    }
}