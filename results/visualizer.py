import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


def visualize_model_learning_result(df: pd.DataFrame, name):
    basePath2 = "dfa/plots"
    dirname = os.path.dirname(f"{basePath2}{name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df = df.melt(
        id_vars=["algorithm", "id", "num_states", "num_alphabet", "distance"],
        var_name="query_type",
        value_name="query_count")
    ax = sns.catplot(kind="box", hue="algorithm", y="query_count", col="query_type", col_wrap=1, data=df,
                     x="distance", sharex=False, sharey=False)
    ax.savefig(f"{basePath2}{name}_distance.png")
    ax = sns.catplot(kind="box", y="query_count", x="algorithm", col="query_type", data=df, col_wrap=1,
                     sharey=False)

    ax.savefig(f"{basePath2}{name}_total.png")
    plt.close()


###########################
#### DFA VISUALIZATION ####
###########################


def visualize_dfa_results():

    basePath = "dfa/data"

    methods = {
        "/DFA_random_learnLib",
        "/DFA_change_tail_learnLib",
        "/DFA_remove_alphabet_learnLib",
        "/DFA_add_alphabet_learnLib",
        "/DFA_add_state_learnLib"
    }

    eqMethod = "/WP"

    for method in methods:
        df = pd.read_csv(basePath + eqMethod + method + "/0005s_5a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0005s_5a")

        df = pd.read_csv(basePath + eqMethod + method + "/0010s_20a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0010s_20a")

        df = pd.read_csv(basePath + eqMethod + method + "/0050s_20a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0050s_20a")

visualize_dfa_results()