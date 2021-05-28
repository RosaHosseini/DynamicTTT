import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


def visualize_model_learning_result(df: pd.DataFrame, name):
    basePath2 = "moore/plots"
    dirname = os.path.dirname(f"{basePath2}{name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df = df.melt(
        id_vars=["algorithm", "id", "num_states", "num_alphabet", "distance"],
        var_name="query_type",
        value_name="query_count")

    ax = sns.catplot(kind="box", y="query_count", x="algorithm", col="query_type", data=df, col_wrap=1,
                     sharey=False)

    ax.savefig(f"{basePath2}{name}_total.png")
    plt.close()


###########################
#### MOORE VISUALIZATION ####
###########################


def visualize_moore_results():
    basePath = "moore/data"

    eqMethod = "/WP"
    df = pd.read_csv(basePath + eqMethod + "/OPEN_SSL_CLIENT.csv")
    visualize_model_learning_result(df, eqMethod + "/OPEN_SSL_CLIENT")

    eqMethod = "/WP"
    df = pd.read_csv(basePath + eqMethod + "/OPEN_SSL_SERVER.csv")
    visualize_model_learning_result(df, eqMethod + "/OPEN_SSL_SERVER")


visualize_moore_results()
