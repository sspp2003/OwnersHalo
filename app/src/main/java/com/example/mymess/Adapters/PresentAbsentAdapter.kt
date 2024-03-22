import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.AbsentItemBinding
import com.example.mymess.databinding.PresentItemBinding

class PresentAbsentAdapter(
    private val items: MutableList<AttendanceItemModel>,
    private val forPresent: Boolean
) : RecyclerView.Adapter<PresentAbsentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (forPresent) {
            PresentItemBinding.inflate(inflater, parent, false)
        } else {
            AbsentItemBinding.inflate(inflater, parent, false)
        }
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return if (forPresent) {
            items.sumBy { it.presentDates?.size ?: 0 }
        } else {
            items.sumBy { it.absentDates?.size ?: 0 }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = if (forPresent) items.flatMap { it.presentDates.orEmpty() }
        else items.flatMap { it.absentDates.orEmpty() }
        holder.bind(item[position])
    }

    inner class ViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
        fun bind(date: String) {
            if (forPresent) {
                val binding = PresentItemBinding.bind(rootView)
                binding.PresentDate.text = date
            } else {
                val binding = AbsentItemBinding.bind(rootView)
                binding.AbsentDate.text = date
            }
        }
    }
}
