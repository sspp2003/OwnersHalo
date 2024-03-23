import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.AbsentItemBinding

class AbsentAdapter(
    private val items: MutableList<AttendanceItemModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AbsentAdapter.AbsentViewHolder>() {

    interface OnItemClickListener {
        fun onDeleteClick(date: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AbsentItemBinding.inflate(inflater, parent, false)
        return AbsentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.sumBy { it.absentDates?.size ?: 0 }
    }

    override fun onBindViewHolder(holder: AbsentViewHolder, position: Int) {
        val absentDates = items.flatMap { it.absentDates.orEmpty() }
        val date = absentDates[position]
        holder.bind(date)
    }

    inner class AbsentViewHolder(private val binding: AbsentItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteDate.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val absentDates = items.flatMap { it.absentDates.orEmpty() }
                    val date = absentDates[position]
                    itemClickListener.onDeleteClick(date)
                }
            }
        }

        fun bind(date: String) {
            binding.AbsentDate.text = date
        }
    }
}
